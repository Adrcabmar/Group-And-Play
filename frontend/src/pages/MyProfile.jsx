/* eslint-disable no-unused-vars */
import React, { useState, useEffect } from "react";
import "../static/resources/css/MyProfile.css";
import Select from 'react-select';
import { useUser } from "../components/UserContext";


function MyProfile() {
  const [isEditing, setIsEditing] = useState(false);
  const { user, setUser } = useUser();
  const [formData, setFormData] = useState(user);
  const [allGames, setAllGames] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  

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
      console.error("⚠ Error al obtener los juegos:", error);
    }
  };

  useEffect(() => {
    setFormData(user);
  }, [user]);

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSave = async () => {
    const { firstname, lastname, email, telephone, favGame, profilePictureUrl } = formData;

    const bodyToSend = {
      firstname, lastname, email, telephone, favGame, profilePictureUrl
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
        throw new Error("Error al guardar los cambios");
      }

      const updatedUser = await response.json();
      setUser(updatedUser);
      setIsEditing(false);
      setSelectedFile(null);
      setPreviewUrl(null);
    } catch (error) {
      console.error("❌ Error al actualizar usuario:", error);
      alert("Ocurrió un error al guardar los cambios.");
    }
  };

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setSelectedFile(file);
    setPreviewUrl(URL.createObjectURL(file));

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch(`${apiUrl}/api/users/${user.id}/upload-photo`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      if (!response.ok) {
        throw new Error("Error al subir la foto");
      }

      const fileName = file.name.replace(/\s+/g, "_");
      const updated = { ...user, profilePictureUrl: `/resources/images/user_${user.id}_${fileName}` };
      setUser(updated);
      setFormData(updated);
    } catch (err) {
      console.error("❌ Error al subir foto:", err);
      alert("Ocurrió un error al subir la foto.");
    }
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
            <button className="profile-btn" onClick={() => setIsEditing(true)}>
              Editar
            </button>
          )}
        </div>

        <div className="profile-right">
          <div className="username-display">{user.username}</div>

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
                <strong>Teléfono:</strong>
                <input
                  type="text"
                  name="telephone"
                  value={formData.telephone || ""}
                  onChange={handleChange}
                  className="profile-input"
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
                />
              </label>
            </>
          ) : (
            <>
              <span><strong>Nombre:</strong> {user.firstname}</span>
              <span><strong>Apellidos:</strong> {user.lastname}</span>
              <span><strong>Email:</strong> {user.email}</span>
              <span><strong>Teléfono:</strong> {user.telephone}</span>
              <span><strong>Juego favorito:</strong> {user.favGame || "No especificado"}</span>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default MyProfile;
