/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import "../static/resources/css/Home.css";
import Select from 'react-select';
import customSelectStyles from "../utils/customSelectStyles";

function Home({ user }) {
  const navigate = useNavigate();
  const [groups, setGroups] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const pageSize = 3;

  const [allGames, setAllGames] = useState([]);
  const [searchGame, setSearchGame] = useState(null);
  const [searchCommunication, setSearchCommunication] = useState("");
  const [searchPlatform, setSearchPlatform] = useState("");
  const platformOptions = [
    { value: "", label: "Cualquier plataforma" },
    { value: "PC", label: "PC" },
    { value: "PLAYSTATION", label: "PlayStation" },
    { value: "XBOX", label: "Xbox" },
    { value: "SWITCH", label: "Switch" },
    { value: "MOBILE", label: "Mobile" },
  ];
  const token = localStorage.getItem("jwt");

  useEffect(() => {
    fetchGames();
  }, []);

  useEffect(() => {
    fetchGroups(page);
  }, [page, searchGame, searchCommunication, searchPlatform]);

  const formatCommunication = (type) => {
    switch (type) {
      case "DISCORD":
        return "Discord";
      case "VOICE_CHAT":
        return "Chat de voz del juego";
      case "NO_COMMUNICATION":
        return "Sin comunicación";
      default:
        return type;
    }
  };

  const clearFilters = () => {
    setSearchGame(null);
    setSearchCommunication("");
    setSearchPlatform("");
    setPage(0);
  };

  const fetchGames = async () => {
    const apiUrl = import.meta.env.VITE_API_URL;

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

  const fetchGroups = async (pageNumber) => {
    const apiUrl = import.meta.env.VITE_API_URL;

    if (!token) {
      console.error("⚠ No hay token almacenado en localStorage");
      return;
    }

    const queryParams = new URLSearchParams({
      page: pageNumber,
      size: pageSize,
      ...(searchGame && { game: searchGame.value }),
      ...(searchCommunication && { communication: searchCommunication }),
      ...(searchPlatform && { platform: searchPlatform }),
    });

    try {
      const response = await fetch(`${apiUrl}/api/groups/open?${queryParams.toString()}`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      });

      if (!response.ok) {
        throw new Error(`Error HTTP: ${response.status}`);
      }

      const data = await response.json();
      setGroups(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error("⚠ Error al obtener los grupos:", error);
    }
  };

  const handleJoinGroup = async (groupId) => {
    const apiUrl = import.meta.env.VITE_API_URL;

    try {
      const response = await fetch(`${apiUrl}/api/groups/join`, {
        method: "PUT",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(groupId),
      });

      if (!response.ok) {
        throw new Error(`Error al unirse al grupo: ${response.status}`);
      }

      const data = await response.json();
      alert(`✅ Te has unido al grupo de: ${data.creatorUsername}`);
      fetchGroups(page);
    } catch (error) {
      console.error("⚠ Error al unirse al grupo:", error);
      alert("❌ No se pudo unir al grupo. Inténtalo más tarde.");
    }
  };

  const formatPlatform = (platform) => {
    switch (platform) {
      case "PC":
        return "PC";
      case "PLAYSTATION":
        return "PlayStation";
      case "XBOX":
        return "Xbox";
      case "SWITCH":
        return "Switch";
      case "MOBILE":
        return "Mobile";
      default:
        return platform;
    }
  };

  return (
    <div className="home-container">
      <aside className="home-left">
      </aside>

      <main className="home-main">
        <h2 className="section-title">Grupos Disponibles</h2>

        <div className="filters d-flex justify-content-center align-items-center gap-2">
          <Select
            className="basic-single"
            classNamePrefix="select"
            isClearable
            isSearchable
            name="game"
            options={allGames}
            placeholder="Selecciona un juego"
            value={searchGame}
            onChange={(selectedOption) => setSearchGame(selectedOption)}
            styles={customSelectStyles}
          />

          <select
            className="custom-select"
            value={searchCommunication}
            onChange={(e) => setSearchCommunication(e.target.value)}
            style={{ maxWidth: "220px" }}
          >
            <option value="">Cualquier comunicación</option>
            <option value="DISCORD">Discord</option>
            <option value="VOICE_CHAT">Chat de voz del juego</option>
            <option value="NO_COMMUNICATION">Sin comunicación</option>
          </select>

          <select
            className="custom-select"
            value={searchPlatform}
            onChange={(e) => setSearchPlatform(e.target.value)}
            style={{ maxWidth: "220px" }}
          >
            {platformOptions.map(opt => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>

          <button className="btn btn-secondary" onClick={clearFilters}>
            Limpiar
          </button>
        </div>

        <div className="groups-wrapper">
          {groups.length > 0 ? (
            <>
              <ul className="group-list">
              {groups.map((group) => (
                <li key={group.id} className="group-card">
                  {/* Header: título a la izquierda, jugadores a la derecha */}
                  <div className="group-header d-flex justify-content-between align-items-center">
                    <h3 className="game-title">{group.gameName}</h3>
                    <span className="players-count">
                      Jugadores: {group.users.length} / {group.maxPlayers}
                    </span>
                  </div>

                  {/* Descripción y Plataforma */}
                  <div className="group-description-block">
                    <p className="group-description">{group.description}</p>
                    <p className="group-platform"><strong>Plataforma:</strong> {formatPlatform(group.platform)}</p>
                  </div>

                  {/* Comunicación y Unirse */}
                  <div className="group-bottom-row">
                    <span className="group-communication">{formatCommunication(group.communication)}</span>
                    <button
                      className="join-button"
                      onClick={() => handleJoinGroup(group.id)}
                      disabled={group.users.length >= group.maxPlayers}
                    >
                      {group.users.length >= group.maxPlayers ? "Lleno" : "Unirse"}
                    </button>
                  </div>
                </li>
              ))}
                {Array.from({ length: 3 - groups.length }).map((_, i) => (
                  <li key={`empty-${i}`} className="empty-card"></li>
                ))}
              </ul>

              <div className="pagination">
                <button disabled={page === 0} onClick={() => setPage(page - 1)}>
                  Anterior
                </button>
                <span>
                  Página {page + 1} de {totalPages}
                </span>
                <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>
                  Siguiente
                </button>
              </div>
            </>
          ) : (
            <p className="no-groups-msg">No hay grupos disponibles.</p>
          )}
        </div>
      </main>

      <aside className="home-right">
      </aside>
    </div>
  );
}

export default Home;
