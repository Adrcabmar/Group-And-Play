import { useState, useEffect } from "react";
import { useUser } from "../../components/UserContext";
import { Navigate } from "react-router-dom";
import "../../static/resources/css/admin/AdminUsers.css";

function AdminUsers() {
  const { user } = useUser();
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState("");
  const [selectedUser, setSelectedUser] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState(null);
  const [changingPassword, setChangingPassword] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const apiUrl = import.meta.env.VITE_API_URL;

  useEffect(() => {
    fetchUsers();
  }, [page, search]);

  const fetchUsers = async () => {
    try {
      const token = localStorage.getItem("jwt");

      let response;
      let data;

      if (search) {
        response = await fetch(`${apiUrl}/api/users/admin/all?page=${page}&size=5&username=${search}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (response.ok) {
          data = await response.json();
          if (data.content.length === 0 && !isNaN(search)) {
            response = await fetch(`${apiUrl}/api/users/admin/all?page=${page}&size=5&id=${search}`, {
              headers: { Authorization: `Bearer ${token}` },
            });
            if (response.ok) {
              data = await response.json();
            }
          }
        }
      } else {
        response = await fetch(`${apiUrl}/api/users/admin/all?page=${page}&size=5`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (response.ok) {
          data = await response.json();
        }
      }

      if (data) {
        setUsers(data.content || []);
        setTotalPages(data.totalPages || 0);
      }
    } catch (error) {
      console.error("Error al cargar usuarios", error);
    }
  };

  const handleOpenModal = (user) => {
    setSelectedUser(user);
    setEditData({ ...user });
    setIsEditing(false);
    setChangingPassword(false);
    setNewPassword("");
  };

  const handleCloseModal = () => {
    setSelectedUser(null);
    setEditData(null);
    setIsEditing(false);
    setChangingPassword(false);
    setNewPassword("");
  };

  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditData(prev => ({ ...prev, [name]: value }));
  };

  const handleSaveChanges = async () => {
    try {
      const token = localStorage.getItem("jwt");
      const { firstname, lastname, username, email, telephone, favGame } = editData;

      const bodyToSend = { firstname, lastname, username, email, telephone, favGame };

      const response = await fetch(`${apiUrl}/api/users/${selectedUser.id}/edit`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(bodyToSend),
      });

      if (!response.ok) {
        const errorMsg = await response.text();
        throw new Error(errorMsg || "Error al actualizar usuario");
      }

      alert("✅ Cambios guardados correctamente");
      setIsEditing(false);
      fetchUsers(); // Refrescamos listado
      handleCloseModal();
    } catch (error) {
      console.error(error);
      alert(error.message || "Error al guardar cambios");
    }
  };

  const handleChangePassword = async () => {
    try {
      const token = localStorage.getItem("jwt");

      const response = await fetch(`${apiUrl}/api/users/${selectedUser.id}/change-password`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ newPassword }),
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || "Error al cambiar contraseña");
      }

      alert("✅ Contraseña cambiada correctamente");
      setChangingPassword(false);
      setNewPassword("");
      handleCloseModal();
    } catch (error) {
      console.error(error);
      alert(error.message || "Error al cambiar contraseña");
    }
  };

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (user.role !== "ADMIN") {
    return <Navigate to="/home" replace />;
  }

  return (
    <div className="admin-users-container">
      <h1 className="admin-users-title">Administrar Usuarios</h1>

      <div className="admin-users-box">
        <div className="admin-users-search">
          <input
            type="text"
            placeholder="Buscar por ID o Username"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            className="admin-users-search-input"
          />
        </div>

        <table className="admin-users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id} onClick={() => handleOpenModal(u)} className="admin-users-row">
                <td>{u.id}</td>
                <td>{u.username}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="admin-users-pagination">
          <button disabled={page === 0} onClick={() => setPage(page - 1)}>
            Anterior
          </button>
          <span className="admin-users-pagination-numbers">{page + 1} / {totalPages}</span>
          <button disabled={page === totalPages - 1} onClick={() => setPage(page + 1)}>
            Siguiente
          </button>
        </div>
      </div>

      {/* Modal */}
      {selectedUser && (
        <div className="user-modal-overlay" onClick={handleCloseModal}>
          <div className="user-modal" onClick={(e) => e.stopPropagation()}>
            {!isEditing && (
              <img
                src={`${apiUrl}${selectedUser.profilePictureUrl || "/resources/images/defecto.png"}`}
                alt="Foto de perfil"
                className="user-profile-pic"
              />)}
            <h2>{isEditing ? "Editar Usuario" : "Detalles de Usuario"}</h2>

            <ul className="user-modal-list">
              <li><strong>ID:</strong> {selectedUser.id}</li>

              <li>
                <strong>Username: </strong>
                {isEditing ? (
                  <input type="text" name="username" value={editData.username || ""} onChange={handleEditChange} className="user-modal-input" />
                ) : selectedUser.username}
              </li>

              <li>
                <strong>Nombre: </strong>
                {isEditing ? (
                  <input type="text" name="firstname" value={editData.firstname || ""} onChange={handleEditChange} className="user-modal-input" />
                ) : selectedUser.firstname}
              </li>

              <li>
                <strong>Apellido: </strong>
                {isEditing ? (
                  <input type="text" name="lastname" value={editData.lastname || ""} onChange={handleEditChange} className="user-modal-input" />
                ) : selectedUser.lastname}
              </li>

              <li>
                <strong>Email: </strong>
                {isEditing ? (
                  <input type="email" name="email" value={editData.email || ""} onChange={handleEditChange} className="user-modal-input" />
                ) : selectedUser.email}
              </li>

              <li>
                <strong>Teléfono: </strong>
                {isEditing ? (
                  <input type="text" name="telephone" value={editData.telephone || ""} onChange={handleEditChange} className="user-modal-input" />
                ) : selectedUser.telephone}
              </li>

              {!isEditing && (
                <li>
                  <strong>Juego Favorito: </strong>
                  {selectedUser.favGame || "N/A"}
                </li>
              )}
            </ul>

            {changingPassword && (
              <div className="change-password-section">
                <h3>Cambiar Contraseña</h3>
                <input
                  type="password"
                  placeholder="Nueva contraseña"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="user-modal-input"
                />
                <div className="user-modal-button-row">
                  <button className="user-modal-close" onClick={handleChangePassword}>
                    Guardar Nueva Contraseña
                  </button>
                  <button className="user-modal-close" onClick={() => {
                    setChangingPassword(false);
                    setNewPassword("");
                  }}>
                    Cancelar
                  </button>
                </div>
              </div>
            )}

            <div className="user-modal-button-row">
              {!isEditing && !changingPassword && (
                <>
                  <button className="user-modal-close" onClick={() => setIsEditing(true)}>Editar</button>
                  <button className="user-modal-close" onClick={() => setChangingPassword(true)}>Cambiar Contraseña</button>
                  <button className="user-modal-close" onClick={handleCloseModal}>Cerrar</button>
                </>
              )}
              {isEditing && (
                <>
                  <button className="user-modal-close" onClick={handleSaveChanges}>Guardar Cambios</button>
                  <button className="user-modal-close" onClick={() => setIsEditing(false)}>Cancelar</button>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminUsers;
