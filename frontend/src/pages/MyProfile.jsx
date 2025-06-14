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
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [confirmAction, setConfirmAction] = useState(() => () => { });
  const [confirmMessage, setConfirmMessage] = useState("");

  const showConfirmation = (message, onConfirm) => {
    setConfirmMessage(message);
    setConfirmAction(() => onConfirm);
    setShowConfirmModal(true);
  };

  useEffect(() => {
    fetchGames();
  }, []);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const discordStatus = params.get("discord");
    if (discordStatus === "success") {
      showAlert("Tu cuenta de Discord ha sido vinculada correctamente");
      fetch(`${apiUrl}/api/users/auth/current-user`, {
        headers: {
          "Authorization": `Bearer ${token}`,
        },
      })
        .then(res => res.json())
        .then(data => {
          const updatedUser = data.user;
          setUser(updatedUser);
          localStorage.setItem("user", JSON.stringify(updatedUser));
        });
    }
    else if (discordStatus === "error") {
      showAlert("Hubo un problema al vincular tu cuenta de Discord.");
    }
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

  const handleSave = () => {
    if (formData.username !== user.username) {
      showConfirmation(
        "Has cambiado tu nombre de usuario. Se cerrará la sesión por seguridad.\n¿Deseas continuar?",
        proceedWithSave
      );
    } else {
      proceedWithSave();
    }
  };

  const proceedWithSave = async () => {
    const { firstname, lastname, email, description, favGame, username } = formData;

    if (description && (description.length < 1 || description.length > 255)) {
      showAlert("La descripción debe tener entre 1 y 255 caracteres.");
      return;
    }

    let profilePictureUrl = formData.profilePictureUrl;

    if (selectedFile) {
      const imageFormData = new FormData();
      imageFormData.append("file", selectedFile);

      try {
        const response = await fetch(`${apiUrl}/api/users/${user.id}/upload-photo`, {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
          body: imageFormData,
        });

        if (!response.ok) throw new Error("Error al subir la foto");

        const fileName = selectedFile.name.replace(/\s+/g, "_");
        profilePictureUrl = `/images/user_${user.id}_${fileName}`;
      } catch (err) {
        console.error("Error al subir foto:", err);
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
      console.error("Error al actualizar usuario:", error);
      showAlert(error.message || "Ocurrió un error al guardar los cambios.");
    }
  };

  const handleUnlinkDiscord = async () => {
    try {
      const res = await fetch(`${apiUrl}/api/auth/discord/unlink`, {
        method: "PATCH",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || "Error al desvincular Discord");
      }

      const resUser = await fetch(`${apiUrl}/api/users/auth/current-user`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      const data = await resUser.json();
      const updatedUser = data.user;

      localStorage.setItem("user", JSON.stringify(updatedUser));
      setUser(updatedUser);
      showAlert("Discord desvinculado correctamente");
    } catch (err) {
      console.error(err);
      showAlert(err.message);
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

      showAlert("Contraseña cambiada correctamente");
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
            src={previewUrl || `${apiUrl}${user.profilePictureUrl || "/resources/images/defecto.png"}`}
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
              <button
                className="profile-btn cancel-btn"
                onClick={() => {
                  setFormData(user);
                  setSelectedFile(null);
                  setPreviewUrl(null);
                  setIsEditing(false);
                }}
              >
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

              {user.discordName ? (
                <button
                  className="profile-btn cancel-btn"
                  style={{ marginTop: "10px" }}
                  onClick={() =>
                    showConfirmation(
                      "¿Estás seguro de que quieres desvincular tu cuenta de Discord?",
                      handleUnlinkDiscord
                    )
                  }
                >
                  Desvincular Discord
                  <img src="/discord-icon.png" alt="Discord" style={{ width: "1.5rem", height: "1.5rem", marginLeft: "0.5rem" }} />
                </button>
              ) : (
                <button
                  className="profile-btn"
                  style={{ marginTop: "10px" }}
                  onClick={() => {
                    const userId = user?.id;
                    if (!userId) {
                      alert("No se pudo obtener el ID de usuario");
                      return;
                    }

                    const redirectUri = encodeURIComponent(`${apiUrl}/api/auth/discord/callback`);
                    const discordUrl =
                      `https://discord.com/api/oauth2/authorize?client_id=1374722658574401596` +
                      `&redirect_uri=${redirectUri}` +
                      `&response_type=code` +
                      `&scope=identify` +
                      `&state=${userId}`;

                    window.location.href = discordUrl;
                  }}
                >
                  Vincular con Discord
                  <img src="/discord-icon.png" alt="Discord" style={{ width: "1.5rem", height: "1.5rem", marginLeft: "0.5rem" }} />
                </button>
              )}
            </>
          )}
        </div>

        <div className="profile-right">
          {isEditing ? (
            <>
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
              <span><strong>Nombre de Discord:</strong> {user.discordName || "Sin vincular"}</span>
              <span><strong>Juego favorito:</strong> {user.favGame || "No especificado"}</span>
            </>
          )}
        </div>
      </div>

      {showPasswordModal && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ backgroundColor: "rgba(31, 26, 51, 0.95)" }}>
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

      {showConfirmModal && (
        <div className="modal">
          <div className="modal-content" style={{ backgroundColor: "rgba(31, 26, 51, 0.95)" }}>
            <h3 className="neon-subtitle" style={{ color: "#00f2ff" }}>¿Quieres continuar?</h3>
            <p style={{ whiteSpace: "pre-line" }}>{confirmMessage}</p>
            <div className="modal-buttons">
              <button
                className="neon-button"
                onClick={() => {
                  confirmAction();
                  setShowConfirmModal(false);
                }}
              >
                Sí
              </button>
              <button
                className="neon-button-secondary"
                onClick={() => setShowConfirmModal(false)}
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
