/* eslint-disable no-unused-vars */
import React, { useEffect, useState } from "react";
import "../static/resources/css/MyGroups.css";

const MyGroups = () => {
  const token = localStorage.getItem("jwt");
  const [groups, setGroups] = useState([]);
  const [selectedGroup, setSelectedGroup] = useState(null);
  const currentUser = JSON.parse(localStorage.getItem("user"));

  useEffect(() => {
    fetchGroups();
  }, []);

  const fetchGroups = async () => {
    const apiUrl = import.meta.env.VITE_API_URL;
    if (!token) return;

    try {
      const res = await fetch(`${apiUrl}/api/groups/my-groups`, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      });
      if (!res.ok) throw new Error("Error al obtener los grupos");
      const data = await res.json();
      setGroups(data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleLeaveOrDelete = (groupId, isCreator) => {
    const apiUrl = import.meta.env.VITE_API_URL;
    const endpoint = isCreator
      ? `/api/groups/delete-my-group/${groupId}`
      : `/api/groups/leave-group/${groupId}`;
    const method = isCreator ? "DELETE" : "PUT";

    fetch(`${apiUrl}${endpoint}`, {
      method,
      headers: { Authorization: `Bearer ${token}` }
    })
      .then((res) => {
        if (res.ok) {
          setGroups((prev) => prev.filter((g) => g.id !== groupId));
          setSelectedGroup(null);
        }
      })
      .catch((err) => console.error(err));
  };

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

  return (
    <div className="my-groups-wrapper">
      <div className="my-groups-container">
        {[...groups, ...Array(6 - groups.length).fill(null)].map((group, idx) =>
          group ? (
            <div
              key={group.id}
              className="group-card"
              onClick={() => setSelectedGroup(group)}
            >
              <h2>{group.gameName}</h2>
            </div>
          ) : (
            <div
              key={`empty-${idx}`}
              className="empty-slot"
              onClick={() => window.location.href = "/"}
            >
              <span>+</span>
            </div>
          )
        )}
      </div>

      {selectedGroup && (
        <div className="modal-overlay" onClick={() => setSelectedGroup(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{selectedGroup.gameName}</h2>
            <p><em>{selectedGroup.description || "Sin descripción"}</em></p>
            <p>Jugadores: {selectedGroup.users.length} / {selectedGroup.maxPlayers}</p>
            <p>
              Creador: <strong>{selectedGroup.creatorUsername}</strong>{" "}
              {selectedGroup.creatorUsername === currentUser.username && (
                <span style={{ color: "#00f2ff" }}> (Tú)</span>
              )}
            </p>
            <p>Comunicación: {TipoComunicacion(selectedGroup.communication)}</p>
            <div className="modal-buttons">
              <button className="modal-close-btn" onClick={() => setSelectedGroup(null)}>Cerrar</button>
              <button className="modal-action-btn"
                onClick={() => handleLeaveOrDelete(selectedGroup.id, selectedGroup.creatorUsername === currentUser.username)}>
                {selectedGroup.creatorUsername === currentUser.username ? "Borrar grupo" : "Abandonar"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyGroups;
