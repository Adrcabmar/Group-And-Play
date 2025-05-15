import { useEffect, useState } from "react";
import Select from "react-select";
import { useUser } from "../../components/UserContext";
import { Navigate } from "react-router-dom";
import "../../static/resources/css/admin/AdminGames.css";
import customMultiSelectStyles from "../../utils/customMultiSelectStyles";
import { useAlert } from "../../components/AlertContext";

const platformOptions = [
  { value: "PC", label: "PC" },
  { value: "PLAYSTATION", label: "PlayStation" },
  { value: "XBOX", label: "Xbox" },
  { value: "SWITCH", label: "Switch" },
  { value: "MOBILE", label: "Mobile" },
];

function AdminGames() {
  const { user } = useUser();
  const [games, setGames] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState("");
  const [selectedGame, setSelectedGame] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState(null);
  const [creating, setCreating] = useState(false);
  const [newGameData, setNewGameData] = useState({ name: "", maxPlayers: "", platforms: [] });
  const apiUrl = import.meta.env.VITE_API_URL;
  const { showAlert } = useAlert();

  useEffect(() => {
    fetchGames();
  }, [page, search]);

  const fetchGames = async () => {
    const token = localStorage.getItem("jwt");
    const params = new URLSearchParams({ page, size: 5 });
    if (search) params.append("gameName", search);
    const response = await fetch(`${apiUrl}/api/games/paginated?${params.toString()}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (response.ok) {
      const data = await response.json();
      setGames(data.content || []);
      setTotalPages(data.totalPages || 0);
    }
  };

  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditData(prev => ({ ...prev, [name]: value }));
  };

  const handlePlatformEditChange = (selectedOptions) => {
    setEditData(prev => ({ ...prev, platforms: selectedOptions.map(opt => opt.value) }));
  };

  const handleSaveChanges = async () => {
    const token = localStorage.getItem("jwt");
    if (!editData.name.trim() || editData.maxPlayers < 2 || editData.maxPlayers > 1023 || editData.platforms.length === 0) {
      showAlert("Datos inválidos. Revisa los campos.");
      return;
    }
    const response = await fetch(`${apiUrl}/api/games/edit`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(editData),
    });
    if (response.ok) {
      showAlert("Juego actualizado");
      setIsEditing(false);
      setSelectedGame(null);
      fetchGames();
    }
  };

  const handleCreateGame = async () => {
    const token = localStorage.getItem("jwt");
    if (!newGameData.name.trim() || newGameData.maxPlayers < 2 || newGameData.maxPlayers > 1023 || newGameData.platforms.length === 0) {
      showAlert("Datos inválidos. Revisa los campos.");
      return;
    }
    const response = await fetch(`${apiUrl}/api/games/create`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        ...newGameData,
        platforms: newGameData.platforms.map(opt => opt.value)
      }),
    });
    if (response.ok) {
      showAlert("Juego creado con éxito");
      setCreating(false);
      setNewGameData({ name: "", maxPlayers: "", platforms: [] });
      fetchGames();
    }
  };

  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== "ADMIN") return <Navigate to="/home" replace />;

  return (
    <div className="admin-games-container">
      <h1 className="admin-games-title">Administrar Juegos</h1>
      <div className="admin-games-box">
        <div className="admin-games-search">
          <input
            type="text"
            placeholder="Buscar por nombre"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            className="admin-games-search-input"
          />
          <button className="admin-games-create-button" onClick={() => setCreating(true)}>Crear Juego</button>
        </div>
        <table className="admin-games-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Máx. Jugadores</th>
              <th>Plataformas</th>
            </tr>
          </thead>
          <tbody>
            {games.map(g => (
              <tr key={g.id} onClick={() => { setSelectedGame(g); setEditData({ ...g }); }} className="admin-games-row">
                <td>{g.id}</td>
                <td>{g.name}</td>
                <td>{g.maxPlayers}</td>
                <td>{g.platforms?.join(", ")}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="admin-games-pagination">
          <button disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</button>
          <span className="admin-games-pagination-numbers">{page + 1} / {totalPages}</span>
          <button disabled={page === totalPages - 1} onClick={() => setPage(page + 1)}>Siguiente</button>
        </div>
      </div>

      {/* Modal Edición */}
      {selectedGame && (
        <div className="game-modal-overlay" onClick={() => setSelectedGame(null)}>
          <div className="game-modal" onClick={(e) => e.stopPropagation()}>
            <h2>{isEditing ? "Editar Juego" : "Detalles del Juego"}</h2>
            <ul className="game-modal-list">
              <li><strong>ID:</strong> {selectedGame.id}</li>
              <li><strong>Nombre:</strong> {isEditing ? (
                <input name="name" value={editData.name} onChange={handleEditChange} className="user-modal-input" />
              ) : selectedGame.name}</li>
              <li><strong>Máx. Jugadores:</strong> {isEditing ? (
                <input name="maxPlayers" type="number" value={editData.maxPlayers} onChange={handleEditChange} className="user-modal-input" />
              ) : selectedGame.maxPlayers}</li>
              <li><strong>Plataformas:</strong> {isEditing ? (
                <Select
                  isMulti
                  options={platformOptions}
                  value={platformOptions.filter(opt => editData.platforms.includes(opt.value))}
                  onChange={handlePlatformEditChange}
                  styles={customMultiSelectStyles}
                />
              ) : selectedGame.platforms?.join(", ")}</li>
            </ul>
            <div className="group-modal-button-row">
              {!isEditing ? (
                <>
                  <button className="group-modal-close" onClick={() => setIsEditing(true)}>Editar</button>
                  <button className="group-modal-close" onClick={() => setSelectedGame(null)}>Cerrar</button>
                </>
              ) : (
                <>
                  <button className="group-modal-close" onClick={handleSaveChanges}>Guardar</button>
                  <button className="group-modal-close" onClick={() => setIsEditing(false)}>Cancelar</button>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Modal Crear Juego */}
      {creating && (
        <div className="game-modal-overlay" onClick={() => setCreating(false)}>
          <div className="game-modal" onClick={(e) => e.stopPropagation()}>
            <h2>Crear Nuevo Juego</h2>
            <input
              placeholder="Nombre"
              name="name"
              value={newGameData.name}
              onChange={(e) => setNewGameData(prev => ({ ...prev, name: e.target.value }))}
              className="user-modal-input"
            />
            <input
              placeholder="Máx. Jugadores"
              type="number"
              name="maxPlayers"
              value={newGameData.maxPlayers}
              onChange={(e) => setNewGameData(prev => ({ ...prev, maxPlayers: parseInt(e.target.value) }))}
              className="user-modal-input"
            />
            <Select
              isMulti
              options={platformOptions}
              value={newGameData.platforms}
              onChange={(selected) => setNewGameData(prev => ({ ...prev, platforms: selected }))}
              styles={customMultiSelectStyles}
            />
            <div className="group-modal-button-row">
              <button className="group-modal-close" onClick={handleCreateGame}>Crear</button>
              <button className="group-modal-close" onClick={() => setCreating(false)}>Cancelar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminGames;