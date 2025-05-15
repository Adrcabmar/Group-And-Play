/* eslint-disable no-unused-vars */
import React, { useState, useEffect } from "react";
import "../static/resources/css/MyProfile.css";
import Select from 'react-select';
import { useUser } from "../components/UserContext";
import customSelectStyles from "../utils/customSelectStyles";
import { useAlert } from "../components/AlertContext";

function MyProfile() {
  const [isEditing, setIsEditing] = useState(false);
  const { user, setUser } = useUser();
  const [formData, setFormData] = useState(user);
  const [allGames, setAllGames] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [passwordForm, setPasswordForm] = useState({ actualPassword: "", newPassword: "" });
  const { showAlert } = useAlert();
  const token = localStorage.getItem("jwt");
  const apiUrl = import.meta.env.VITE_API_URL;

  useEffect(() => {
    fetchGames();
  }, []);

  const fetchGames = async () => {
    try {
      const response = await fetch(`${apiUrl}/api/games/all`, {
        headers: {
          "Authorization": `Bearer ${token}`,
        }
      });
      const data = await response.json();
      const options = data.map(game => ({
        value: game.name,
        label: game.name
      }));
      setAllGames(options);
    } catch (error) {
      console.error(" Error al obtener los juegos:", error);
    }
  };

  useEffect(() => {
    setFormData(user);
  }, [user]);

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSave = async () => {
    const { firstname, lastname, email, description, favGame, username } = formData;

    if (description && (description.length < 1 || description.length > 256)) {
      showAlert("La descripción debe tener entre 1 y 256 caracteres.");
      return;
    }

    let profilePictureUrl = formData.profilePictureUrl;

    if (selectedFile) {
      const imageFormData = new FormData();
      imageFormData.append("file", selectedFile);

      try {
        const response = await fetch(`${apiUrl}/api/users/${user.id}/upload-photo`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: imageFormData,
        });

        if (!response.ok) {
          throw new Error("Error al subir la foto");
        }

        const fileName = selectedFile.name.replace(/\s+/g, "_");
        profilePictureUrl = `/resources/images/user_${user.id}_${fileName}`;
      } catch (err) {
        console.error(" Error al subir foto:", err);
        showAlert("Ocurrió un error al subir la foto.");
        return;
      }
    }

    const bodyToSend = {
      firstname,
      lastname,
      email,
      description,
      favGame,
      profilePictureUrl,
      username,
    };

    if (username !== user.username) {
      const confirmLogout = window.confirm("Has cambiado tu nombre de usuario. Se cerrará la sesión por seguridad.\n¿Deseas continuar?");
      if (!confirmLogout) return;
    }

    try {
      const response = await fetch(`${apiUrl}/api/users/${user.id}/edit`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(bodyToSend),
      });

      if (!response.ok) {
        const errorMsg = await response.text();
        throw new Error(errorMsg || "Error al guardar los cambios");
      }

      const result = await response.json();
      const updatedUser = result.user;

      if (result.usernameChanged) {
        showAlert("Has cambiado tu nombre de usuario. Por seguridad, se cerrará la sesión.");
        localStorage.removeItem("jwt");
        localStorage.removeItem("user");
        window.location.href = "/login";
      } else {
        setTimeout(() => {
          setUser(updatedUser);
          localStorage.setItem("user", JSON.stringify(updatedUser));
          setIsEditing(false);
          setSelectedFile(null);
          setPreviewUrl(null);
          showAlert("Cambios guardados correctamente");
        }, 500);
      }

    } catch (error) {
      console.error(" Error al actualizar usuario:", error);
      showAlert(error.message || "Ocurrió un error al guardar los cambios.");
    }
  };


  const handlePasswordChange = async () => {
    try {
      const response = await fetch(`${apiUrl}/api/users/${user.id}/change-password`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(passwordForm),
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || "Error al cambiar la contraseña");
      }

      showAlert(" Contraseña cambiada correctamente");
      setShowPasswordModal(false);
      setPasswordForm({ actualPassword: "", newPassword: "" });

    } catch (error) {
      console.error(error);
      showAlert(error.message);
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const allowedTypes = ["image/jpg", "image/jpeg", "image/png", "image/webp"];
    if (!allowedTypes.includes(file.type)) {
      showAlert("Formato no permitido. Solo se aceptan imágenes JPG, PNG o WEBP.");
      return;
    }

    setSelectedFile(file);
    setPreviewUrl(URL.createObjectURL(file));
  };


  return (
    <div className="profile-container">
      <div className="profile-box">
        <div className="profile-left">
          <img
            src={previewUrl || `http://localhost:8080${user.profilePictureUrl || "/resources/images/defecto.png"}`}
            alt="Foto de perfil"
            className="profile-pic"
          />

          {isEditing && (
            <>
              <input
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                className="profile-input"
                style={{ marginTop: "1rem" }}
              />
              {previewUrl && (
                <span style={{ fontSize: "0.9rem", color: "#555", marginTop: "0.5rem" }}>
                  Vista previa activa
                </span>
              )}
            </>
          )}

          {isEditing ? (
            <div className="button-row">
              <button className="profile-btn" onClick={handleSave}>
                Guardar
              </button>
              <button className="profile-btn cancel-btn" onClick={() => {
                setFormData(user);
                setSelectedFile(null);
                setPreviewUrl(null);
                setIsEditing(false);
              }}>
                Cancelar
              </button>
            </div>
          ) : (
            <>
              <button className="profile-btn" onClick={() => setIsEditing(true)}>
                Editar
              </button>
              <button
                className="profile-btn neon-outline-btn"
                style={{ marginTop: "10px" }}
                onClick={() => setShowPasswordModal(true)}
              >
                Cambiar contraseña
              </button>
            </>
          )}
        </div>

        <div className="profile-right">
          {isEditing ? (
            <label className="field-label">
              <strong>Nombre de usuario:</strong>
              <input
                type="text"
                name="username"
                value={formData.username || ""}
                onChange={handleChange}
                className="profile-input"
              />
            </label>
          ) : (
            <div className="username-display">{user.username}</div>
          )}

          {isEditing ? (
            <>
              <label className="field-label">
                <strong>Nombre:</strong>
                <input
                  type="text"
                  name="firstname"
                  value={formData.firstname || ""}
                  onChange={handleChange}
                  className="profile-input"
                />
              </label>

              <label className="field-label">
                <strong>Apellidos:</strong>
                <input
                  type="text"
                  name="lastname"
                  value={formData.lastname || ""}
                  onChange={handleChange}
                  className="profile-input"
                />
              </label>

              <label className="field-label">
                <strong>Email:</strong>
                <input
                  type="email"
                  name="email"
                  value={formData.email || ""}
                  onChange={handleChange}
                  className="profile-input"
                />
              </label>

              <label className="field-label">
                <strong>Descripción:</strong>
                <textarea
                  name="description"
                  value={formData.description || ""}
                  onChange={handleChange}
                  className="profile-input"
                  maxLength={256}
                  placeholder="Cuéntanos algo sobre ti ..."
                  rows={4}
                />
              </label>

              <label className="field-label">
                <strong>Juego favorito:</strong>
                <Select
                  options={allGames}
                  value={allGames.find(option => option.value === formData.favGame) || null}
                  onChange={(selected) => setFormData(prev => ({ ...prev, favGame: selected?.value || "" }))}
                  placeholder="Selecciona un juego..."
                  isClearable
                  styles={customSelectStyles}
                />
              </label>
            </>
          ) : (
            <>
              <span><strong>Nombre:</strong> {user.firstname}</span>
              <span><strong>Apellidos:</strong> {user.lastname}</span>
              <span><strong>Email:</strong> {user.email}</span>
              <span className="profile-description"><strong>Descripción:</strong> {user.description || "Sin descripción"}</span>
              <span><strong>Juego favorito:</strong> {user.favGame || "No especificado"}</span>
            </>
          )}
        </div>
      </div>

      {showPasswordModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>Cambiar contraseña</h2>

            <label>Contraseña actual:</label>
            <input
              type="password"
              value={passwordForm.actualPassword}
              onChange={(e) =>
                setPasswordForm((prev) => ({ ...prev, actualPassword: e.target.value }))
              }
              className="profile-input"
            />

            <label>Nueva contraseña:</label>
            <input
              type="password"
              value={passwordForm.newPassword}
              onChange={(e) =>
                setPasswordForm((prev) => ({ ...prev, newPassword: e.target.value }))
              }
              className="profile-input"
            />

            <div className="button-row">
              <button className="profile-btn" onClick={handlePasswordChange}>
                Guardar
              </button>
              <button
                className="profile-btn cancel-btn"
                onClick={() => {
                  setPasswordForm({ actualPassword: "", newPassword: "" });
                  setShowPasswordModal(false);
                }}
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MyProfile;