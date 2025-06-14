/* eslint-disable no-unused-vars */
import { useNavigate } from "react-router-dom";
import React, { useState, useEffect } from 'react';
import Select from 'react-select';
import customSelectStyles from "../utils/customSelectStyles";
import "../static/resources/css/CreateGroup.css";

function CrearGrupo() {
  const navigate = useNavigate();
  const currentUser = JSON.parse(localStorage.getItem("user"));
  const token = localStorage.getItem("jwt");
  const apiUrl = import.meta.env.VITE_API_URL;

  const [allGames, setAllGames] = useState([]);
  const [selectedGame, setSelectedGame] = useState(null);
  const [platformOptions, setPlatformOptions] = useState([]);
  const [selectedPlatform, setSelectedPlatform] = useState("");
  const [status, setStatus] = useState("OPEN");

  const [form, setForm] = useState({
    communication: "VOICE_CHAT",
    description: "",
    usergame: ""
  });

  const [error, setError] = useState(null);

  useEffect(() => {
    fetchGames();
  }, []);

  const fetchGames = async () => {
    try {
      const response = await fetch(`${apiUrl}/api/games/all`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const data = await response.json();
      const options = data.map(game => ({
        value: game.name,
        label: game.name,
        platforms: game.platforms
      }));
      setAllGames(options);
    } catch (err) {
      console.error("⚠ Error al cargar juegos:", err);
    }
  };

  const handleGameChange = (selectedOption) => {
    setSelectedGame(selectedOption);
    setPlatformOptions(selectedOption ? selectedOption.platforms : []);
    setSelectedPlatform("");
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!token) {
      setError("⚠ No hay token. Inicia sesión nuevamente.");
      return;
    }

    if (!selectedGame || !selectedPlatform || !form.description || !form.usergame) {
      setError("⚠ Todos los campos son obligatorios.");
      return;
    }

    const groupData = {
      communication: form.communication,
      description: form.description,
      platform: selectedPlatform,
      usergame: form.usergame,
      gameName: selectedGame.value,
      creatorId: currentUser?.id,
      status: status,
    };

    try {
      const response = await fetch(`${apiUrl}/api/groups/create`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(groupData)
      });

      const responseData = await response.json();

      if (!response.ok) {
        setError("Error: " + (responseData.error || "No se pudo crear el grupo"));
        return;
      }

      navigate("/");

    } catch (error) {
      console.error("⚠ Error al crear grupo:", error);
      setError("Error al crear grupo. Inténtalo de nuevo.");
    }
  };

  return (
    <div className="login-container">
      <div className="login-box" style={{ width: "30%", padding: "1.5rem", marginTop: "3rem" }}>
        <h2>Crear grupo</h2>
        {error && <p style={{ color: "red" }}>{error}</p>}
        <form onSubmit={handleSubmit}>
          <div style={{ display: "flex", justifyContent: "center" }}>
            <Select
              isSearchable
              name="game"
              options={allGames}
              placeholder="Selecciona un juego"
              value={selectedGame}
              onChange={handleGameChange}
              required
              styles={customSelectStyles}
              menuPortalTarget={document.body}
            />
          </div>

          <select
            value={selectedPlatform}
            onChange={(e) => {
              if (!selectedGame) return;
              setSelectedPlatform(e.target.value);
            }}
            className="custom-select"
            style={{
              marginTop: "10px",
              minWidth: "100%",
              color: !selectedGame ? "#888" : "inherit",
              cursor: !selectedGame ? "not-allowed" : "pointer",
              backgroundColor: "#111",
            }}
          >
            <option value="">
              {!selectedGame
                ? "Elige un juego antes de elegir plataforma"
                : "Selecciona una plataforma"}
            </option>
            {selectedGame &&
              platformOptions.map((platform) => (
                <option key={platform} value={platform}>
                  {platform}
                </option>
              ))}
          </select>

          <input
            type="text"
            name="usergame"
            placeholder="Nombre de usuario en el juego/plataforma"
            value={form.usergame}
            onChange={handleChange}
            required
          />

          <input
            type="text"
            name="description"
            placeholder="Descripción"
            value={form.description}
            onChange={handleChange}
            required
          />

          <select
            name="communication"
            value={form.communication}
            onChange={handleChange}
            required
            className="custom-select"
            style={{ minWidth: "100%" }}
          >
            <option value="VOICE_CHAT">Chat de voz del juego</option>
            <option value="NO_COMMUNICATION">Sin comunicación</option>
            <>
            {currentUser.discordName? 
            <option value="DISCORD">Discord</option>: null}</>
          </select>

          <select
            className="custom-select"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            style={{ minWidth: "100%" }}

          >
            <option value="OPEN">Grupo Abierto </option>
            <option value="CLOSED">Grupo Cerrado </option>
          </select>

          <button type="submit">Crear grupo</button>
        </form>
      </div>
    </div>
  );
}

export default CrearGrupo;
