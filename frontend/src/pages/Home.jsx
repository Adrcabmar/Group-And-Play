/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import "../static/resources/css/Home.css";
import Select from 'react-select';

function Home({ user }) {
  const navigate = useNavigate();
  const [groups, setGroups] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const pageSize = 3;

  const [allGames, setAllGames] = useState([]);
  const [searchGame, setSearchGame] = useState(null); // objeto de react-select
  const [searchCommunication, setSearchCommunication] = useState("");

  const token = localStorage.getItem("jwt");

  useEffect(() => {
    fetchGames();
  }, []);

  useEffect(() => {
    fetchGroups(page);
  }, [page, searchGame, searchCommunication]);

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

  return (
    <div className="home-container">
      <aside className="home-left">
        <p>IZQUIERDA</p>
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
            styles={{
              container: (base) => ({ ...base, width: 300 }),
            }}
          />

          <select
            className="form-select"
            value={searchCommunication}
            onChange={(e) => setSearchCommunication(e.target.value)}
            style={{ maxWidth: "220px" }}
          >
            <option value="">Cualquier comunicación</option>
            <option value="DISCORD">Discord</option>
            <option value="VOICE_CHAT">Chat de voz del juego</option>
            <option value="NO_COMMUNICATION">Sin comunicación</option>
          </select>
          
          <button className="btn btn-secondary" onClick={clearFilters}>
            Limpiar
          </button>
        </div>

        <div className="groups-wrapper">
          {groups.length > 0 ? (
            <ul className="group-list">
              {groups.map((group) => (
                <li key={group.id} className="group-card">
                  <div className="group-header">
                    <h3 className="game-title">{group.gameName}</h3>
                    <span className="players-count">
                      Jugadores: {group.users.length} / {group.maxPlayers}
                    </span>
                  </div>
                  <p className="group-description">{group.description}</p>
                  <div className="group-bottom-row">
                    <span className="group-communication">{formatCommunication(group.communication)}</span>
                    <button
                      className="btn btn-success join-button"
                      onClick={() => handleJoinGroup(group.id)}
                      disabled={group.users.length >= group.maxPlayers}
                    >
                      {group.users.length >= group.maxPlayers ? "Lleno" : "Unirse"}
                    </button>
                  </div>
                </li>
              ))}
              {Array.from({ length: 3 - groups.length }).map((_, i) => (
                <li key={`empty-${i}`} className="group-card empty-card"></li>
              ))}
            </ul>
          ) : (
            <p>No hay grupos disponibles.</p>
          )}
        </div>
      </main>

      <div
        className="pagination"
        style={{
          display: "flex",
          justifyContent: "center",
          marginTop: "20px",
          gridColumn: "2",
        }}
      >
        <button disabled={page === 0} onClick={() => setPage(page - 1)}>
          Anterior
        </button>
        <span style={{ margin: "0 10px", lineHeight: "32px" }}>
          Página {page + 1} de {totalPages}
        </span>
        <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>
          Siguiente
        </button>
      </div>

      <aside className="home-right">
        <button className="create-group-btn btn btn-primary" onClick={() => navigate("/create-group")}>
          Crear Grupo
        </button>
      </aside>
    </div>
  );
}

export default Home;
