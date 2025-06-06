import { useState, useEffect } from "react";
import { useUser } from "../../components/UserContext";
import { Navigate } from "react-router-dom";
import Select from "react-select";
import "../../static/resources/css/admin/AdminGroups.css";
import customSelectStyles from "../../utils/customSelectStyles";
import { useAlert } from "../../components/AlertContext";

function AdminGroups() {
  const { user } = useUser();
  const [groups, setGroups] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchId, setSearchId] = useState("");
  const [searchGame, setSearchGame] = useState(null);
  const [searchStatus, setSearchStatus] = useState("");
  const [allGames, setAllGames] = useState([]);
  const [selectedGroup, setSelectedGroup] = useState(null);
  const apiUrl = import.meta.env.VITE_API_URL;
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState(null);
  const { showAlert } = useAlert();

  useEffect(() => {
    fetchGames();
  }, []);

  useEffect(() => {
    fetchGroups();
  }, [page, searchId, searchGame, searchStatus]);

  const fetchGames = async () => {
    const token = localStorage.getItem("jwt");
    try {
      const response = await fetch(`${apiUrl}/api/games/all`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await response.json();
      const options = data.map((game) => ({
        value: game.name,
        label: game.name,
      }));
      setAllGames(options);
    } catch (error) {
      console.error("⚠ Error al obtener los juegos:", error);
    }
  };

  const fetchGroups = async () => {
    try {
      const token = localStorage.getItem("jwt");

      const params = new URLSearchParams({
        page,
        size: 5,
      });
      if (searchId) params.append("id", searchId);
      if (searchGame) params.append("game", searchGame.value);
      if (searchStatus) params.append("status", searchStatus);

      const response = await fetch(`${apiUrl}/api/groups/admin/all?${params.toString()}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (response.ok) {
        const data = await response.json();
        setGroups(data.content || []);
        setTotalPages(data.totalPages || 0);
      }
    } catch (error) {
      console.error("Error al cargar grupos", error);
    }
  };

  const handleClearFilters = () => {
    setSearchId("");
    setSearchGame(null);
    setSearchStatus("");
    setPage(0);
  };

  const handleOpenModal = (group) => {
    setSelectedGroup(group);
    setEditData({ ...group });
    setIsEditing(false);
  };
  const handleCloseModal = () => setSelectedGroup(null);

  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditData(prev => ({ ...prev, [name]: value }));
  };

  const handleSaveChanges = async () => {
    try {
      const token = localStorage.getItem("jwt");

      const bodyToSend = {
        status: editData.status,
        communication: editData.communication,
        usergame: editData.usergame,
        platform: editData.platform,
        description: editData.description
      };

      const response = await fetch(`${apiUrl}/api/groups/edit/${selectedGroup.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(bodyToSend),
      });

      if (!response.ok) {
        const errorMsg = await response.text();
        throw new Error(errorMsg || "Error al actualizar el grupo");
      }

      showAlert("Cambios guardados correctamente");
      setIsEditing(false);
      fetchGroups();
      setSelectedGroup(null);
    } catch (error) {
      console.error(error);
      showAlert("Error al guardar cambios");
    }
  };

  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== "ADMIN") return <Navigate to="/home" replace />;


  const TipoComunicacion = (tipo) => {
    switch (tipo) {
      case "DISCORD":
        return "Discord";
      case "VOICE_CHAT":
        return "Chat de voz del juego";
      case "NO_COMMUNICATION":
        return "Sin comunicación";
      default:
        return tipo;
    }
  };

  const TipoEstado = (tipo) => {
    switch (tipo) {
      case "OPEN":
        return "Abierto";
      case "CLOSED":
        return "Cerrado";
      case "FINISHED":
        return "Finalizado";
      default:
        return tipo;
    }
  };

  return (
    <div className="admin-groups-container">
      <h1 className="admin-groups-title">Administrar Grupos</h1>

      <div className="admin-groups-box">
        <div className="admin-groups-search">
          <input
            type="text"
            placeholder="Buscar por ID"
            value={searchId}
            onChange={(e) => {
              setSearchId(e.target.value);
              setPage(0);
            }}
            className="admin-groups-search-input"
            style={{ maxWidth: "180px", marginRight: "1rem" }}
          />

          <Select
            className="basic-single"
            classNamePrefix="select"
            isClearable
            isSearchable
            name="game"
            options={allGames}
            placeholder="Selecciona un juego"
            value={searchGame}
            onChange={(selectedOption) => {
              setSearchGame(selectedOption);
              setPage(0);
            }}
            styles={customSelectStyles}
          />

          <select
            className="custom-select"
            value={searchStatus}
            onChange={(e) => {
              setSearchStatus(e.target.value);
              setPage(0);
            }}
            style={{ maxWidth: "200px", marginRight: "1rem" }}
          >
            <option value="">Cualquier estado</option>
            <option value="OPEN">Abierto</option>
            <option value="CLOSED">Cerrado</option>
            <option value="FINISHED">Finalizado</option>
          </select>

          <button className="group-modal-close" onClick={handleClearFilters}>
            Limpiar filtros
          </button>
        </div>

        <table className="admin-groups-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Juego</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {groups.map((g) => (
              <tr key={g.id} onClick={() => handleOpenModal(g)} className="admin-groups-row">
                <td>{g.id}</td>
                <td>{g.gameName}</td>
                <td>{g.status}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="admin-groups-pagination">
          <button disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</button>
          <span className="admin-groups-pagination-numbers">{page + 1} / {totalPages}</span>
          <button disabled={page === totalPages - 1} onClick={() => setPage(page + 1)}>Siguiente</button>
        </div>
      </div>

      {selectedGroup && (
        <div className="group-modal-overlay" onClick={handleCloseModal}>
          <div className="group-modal" onClick={(e) => e.stopPropagation()}>
            <h2>{isEditing ? "Editar Grupo" : "Detalles del Grupo"}</h2>

            <ul className="group-modal-list">
              <li><strong>ID:</strong> {selectedGroup.id}</li>
              <li><strong>Juego:</strong> {selectedGroup.gameName || "Sin juego"}</li>

              <li>
                <strong>Estado:</strong>{" "}
                {isEditing ? (
                  <select name="status" value={editData.status} onChange={handleEditChange} className="user-modal-input">
                    <option value="OPEN">Abierto</option>
                    <option value="CLOSED">Cerrado</option>
                    <option value="FINISHED">Finalizado</option>
                  </select>
                ) : (
                  TipoEstado(selectedGroup.status)
                )}
              </li>

              <li>
                <strong>Comunicación:</strong>{" "}
                {isEditing ? (
                  <select name="communication" value={editData.communication} onChange={handleEditChange} className="user-modal-input">
                    <option value="DISCORD">Discord</option>
                    <option value="VOICE_CHAT">Chat de voz del juego</option>
                    <option value="NO_COMMUNICATION">Sin comunicación</option>
                  </select>
                ) : (
                  TipoComunicacion(selectedGroup.communication)
                )}
              </li>

              <li>
                <strong>Usuario en juego:</strong>{" "}
                {isEditing ? (
                  <input type="text" name="usergame" value={editData.usergame} onChange={handleEditChange} className="user-modal-input" />
                ) : (
                  selectedGroup.usergame
                )}
              </li>

              <li>
                <strong>Plataforma:</strong>{" "}
                {isEditing ? (
                  <select name="platform" value={editData.platform} onChange={handleEditChange} className="user-modal-input">
                    <option value="PC">PC</option>
                    <option value="PLAYSTATION">PlayStation</option>
                    <option value="XBOX">Xbox</option>
                    <option value="NINTENDO">Nintendo</option>
                  </select>
                ) : (
                  selectedGroup.platform
                )}
              </li>

              <li>
                <strong>Descripción:</strong>{" "}
                {isEditing ? (
                  <textarea
                    name="description"
                    value={editData.description}
                    onChange={handleEditChange}
                    className="user-modal-input"
                    rows={3}
                  />
                ) : (
                  selectedGroup.description || "Sin descripción"
                )}
              </li>
              <li><strong>Fecha de creación:</strong> {new Date(selectedGroup.creation).toLocaleString()}</li>
            </ul>

            <div className="group-modal-button-row">
              {!isEditing ? (
                <>
                  <button className="group-modal-close" onClick={() => setIsEditing(true)}>Editar</button>
                  <button className="group-modal-close" onClick={handleCloseModal}>Cerrar</button>
                </>
              ) : (
                <>
                  <button className="group-modal-close" onClick={handleSaveChanges}>Guardar Cambios</button>
                  <button className="group-modal-close" onClick={() => setIsEditing(false)}>Cancelar</button>
                </>
              )}
            </div>
          </div>
        </div>

      )}
    </div>
  );
}

export default AdminGroups;
