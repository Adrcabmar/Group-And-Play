/* eslint-disable no-unused-vars */
import { Link, useNavigate } from "react-router-dom";
import React, { useState } from 'react';
import axios from "axios";

function CrearGrupo() {
  const navigate = useNavigate();
  const currentUser = JSON.parse(localStorage.getItem("user"));
  const token = localStorage.getItem("jwt"); 
  const apiUrl = import.meta.env.VITE_API_URL

  const [form, setForm] = useState({
      communication: "VOICE_CHAT",
      description: "",
      gameName: "",
      creatorId: currentUser?.id,
  });

  const [error, setError] = useState(null);

  const handleChange = (e) => {
      setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
      e.preventDefault();
      console.log("Enviando datos del formulario:", form); 

      if (!token) {
          setError("⚠ No hay token. Inicia sesión nuevamente.");
          return;
      }

      const groupData = {
          communication: form.communication,
          description: form.description,
          gameName: form.gameName,
          creatorId: currentUser?.id, 
      };

      try {
          const response = await fetch(`${apiUrl}/api/groups/create`, {
              method: "POST",
              headers: {
                  "Authorization": `Bearer ${token}`,
                  "Content-Type": "application/json"
              },
              body: JSON.stringify(groupData),
          });

          console.log("🔹 Respuesta del servidor:", response.status);

          const responseData = await response.json();
          
          if (!response.ok) {
              setError("Error: " + (responseData.error || "No se pudo crear el grupo"));
              return;
          }

          console.log("✅ Grupo creado exitosamente:", responseData);
          navigate("/");

      } catch (error) {
          console.error("⚠ Error en el registro:", error);
          setError("Error al crear grupo. Inténtalo de nuevo.");
      }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h2>Crear grupo</h2>
        {error && <p style={{ color: "red" }}>{error}</p>}
        <form onSubmit={handleSubmit}>
           <input type="text" name="gameName" placeholder="Juego" value={form.gameName} onChange={handleChange} required />
           <input type="text" name="description" placeholder="Descrición" value={form.description} onChange={handleChange} required />
           <select name="communication" value={form.communication} onChange={handleChange} required
            style={{
                padding: "10px",
                borderRadius: "5px",
                border: "1px solid #ccc",
                backgroundColor: "#f9f9f9",
                cursor: "pointer",
                fontSize: "16px",
                outline: "none",
                transition: "0.3s ease-in-out",
            }}>
                <option value="VOICE_CHAT">Chat de voz del juego</option>
                <option value="NO_COMMUNICATION">Sin comunicación</option>
                <option value="DISCORD">Discord</option>
            </select>
          <button type="submit">Crear grupo</button>
        </form>
      </div>
    </div>
  );
}
export default CrearGrupo;