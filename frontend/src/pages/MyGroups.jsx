/* eslint-disable no-unused-vars */
import React, { useEffect, useState } from "react";
import "../static/resources/css/MyGroups.css";
import { useAlert } from "../components/AlertContext";

const MyGroups = () => {
  const token = localStorage.getItem("jwt");
  const [groups, setGroups] = useState([]);
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const { showAlert } = useAlert();
  const currentUser = JSON.parse(localStorage.getItem("user"));
  const [selectedGame, setSelectedGame] = useState(null);
  const [friends, setFriends] = useState([]);
  const [showInviteDropdown, setShowInviteDropdown] = useState(false);
  const [selectedFriend, setSelectedFriend] = useState("");

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
      console.log("Grupos obtenidos:", data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchFriends = async () => {
    try {
      const res = await fetch(`${import.meta.env.VITE_API_URL}/api/users/friends/all?size=100`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const data = await res.json();
      setFriends(data.content || []);
    } catch (err) {
      console.error("Error al cargar amigos:", err);
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

  const handleSaveEdit = async (groupData) => {
    const apiUrl = import.meta.env.VITE_API_URL;
    try {
      const res = await fetch(`${apiUrl}/api/groups/edit/${groupData.id}`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          platform: groupData.platform,
          description: groupData.description,
          usergame: groupData.usergame,
          status: groupData.status,
          communication: groupData.communication
        })
      });

      if (!res.ok) throw new Error("Error al editar el grupo");

      const updated = await res.json();
      setGroups((prev) =>
        prev.map((g) => (g.id === updated.id ? updated : g))
      );
      setSelectedGroup(updated);
      setEditMode(false);
    } catch (err) {
      console.error("Error editando grupo:", err);
      showAlert("No se pudo guardar el grupo");
    }
  };

  const fetchGameByName = async (name) => {
    const apiUrl = import.meta.env.VITE_API_URL;
    try {
      const encodedName = encodeURIComponent(name);
      const res = await fetch(`${apiUrl}/api/games/find/${encodedName}`, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      });
      if (res.ok) {
        const game = await res.json();
        setSelectedGame(game);
      } else {
        setSelectedGame(null);
        console.warn("No se pudo obtener el juego");
      }
    } catch (err) {
      console.error(err);
      setSelectedGame(null);
    }
  };

  const handleInvite = async (username, groupId) => {
    try {
      await fetch(`${import.meta.env.VITE_API_URL}/api/invitations/create`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          receiverUsername: username,
          groupInvitation: true,
          groupId: groupId
        })
      });
      showAlert(`Invitación enviada a ${username}`);
      setSelectedFriend("");
    } catch (err) {
      showAlert("Error al invitar: " + (await err.response?.text()));
    }
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
              onClick={() => {
                setSelectedGroup(group);
                setEditMode(false);
                fetchGameByName(group.gameName);
                fetchFriends();
                setShowInviteDropdown(false);
              }}
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
          <div className="modal-content" style={{backgroundColor: "rgba(31, 26, 51, 1)"}} onClick={(e) => e.stopPropagation()}>
            <h2>{selectedGroup.gameName}</h2>

            {editMode ? (
              <>
                <label>Descripción:</label>
                <textarea
                  value={selectedGroup.description}
                  onChange={(e) =>
                    setSelectedGroup({
                      ...selectedGroup,
                      description: e.target.value
                    })
                  }
                />

                <label>Plataforma:</label>
                <select
                  value={selectedGroup.platform}
                  onChange={(e) =>
                    setSelectedGroup({
                      ...selectedGroup,
                      platform: e.target.value
                    })
                  }
                >
                  {selectedGame?.platforms?.map((p) => (
                    <option key={p} value={p}>{p}</option>
                  ))}
                </select>

                <label>Usuario en la plataforma:</label>
                <input
                  type="text"
                  value={selectedGroup.usergame}
                  style={{ color: "#00f2ff" }}
                  onChange={(e) =>
                    setSelectedGroup({
                      ...selectedGroup,
                      usergame: e.target.value
                    })
                  }
                />

                <label>Estado del grupo:</label>
                <select
                  value={selectedGroup.status}
                  onChange={(e) =>
                    setSelectedGroup({
                      ...selectedGroup,
                      status: e.target.value
                    })
                  }
                >
                  <option value="OPEN">Abierto</option>
                  <option value="CLOSED">Cerrado</option>
                </select>

                <label>Comunicación:</label>
                <select
                  value={selectedGroup.communication}
                  onChange={(e) =>
                    setSelectedGroup({
                      ...selectedGroup,
                      communication: e.target.value
                    })
                  }
                >
                  <>
                    {currentUser.discordName ?
                      <option value="DISCORD">Discord</option> : null}
                  </>
                  <option value="VOICE_CHAT">Chat de voz del juego</option>
                  <option value="NO_COMMUNICATION">Sin comunicación</option>
                </select>

                <div className="modal-buttons">
                  <button
                    className="modal-action-btn"
                    onClick={() => handleSaveEdit(selectedGroup)}
                  >
                    Guardar cambios
                  </button>
                  <button
                    className="modal-cancel-btn"
                    onClick={() => setEditMode(false)}
                  >
                    Cancelar
                  </button>
                </div>
              </>
            ) : (
              <>
                <p className="description-text"><em>{selectedGroup.description || "Sin descripción"}</em></p>
                <p>Jugadores: {selectedGroup.users.length} / {selectedGroup.maxPlayers}</p>
                <p>
                  Creador: <strong>{selectedGroup.creatorUsername}</strong>{" "}
                  {selectedGroup.creatorUsername === currentUser.username && (
                    <span style={{ color: "#00f2ff" }}> (Tú)</span>
                  )}
                </p>
                <p>
                  Comunicación: <span className="info-highlight">{TipoComunicacion(selectedGroup.communication)}</span>
                  {selectedGroup.communication === "DISCORD" && selectedGroup.discordName && (
                    <>
                      {" "}
                      - <span className="info-highlight">{selectedGroup.discordName}</span>
                    </>
                  )}
                </p>
                <p>Plataforma: <strong>{selectedGroup.platform}</strong></p>
                <p>Usuario en la plataforma: <strong>{selectedGroup.usergame}</strong></p>

                {selectedGroup.creatorUsername === currentUser.username && (
                  <>
                    <button className="modal-edit-btn" onClick={() => setEditMode(true)} style={{ marginBottom: "0.5rem" }}>
                      Editar grupo
                    </button>

                    {!showInviteDropdown && (
                      <button
                        className="modal-edit-btn"
                        onClick={() => setShowInviteDropdown(!showInviteDropdown)}
                      >
                        Invitar
                      </button>)
                    }

                    {showInviteDropdown && (
                      <div style={{ marginTop: "1rem" }}>
                        <select
                          className="custom-select"
                          value={selectedFriend}
                          onChange={(e) => setSelectedFriend(e.target.value)}
                        >
                          <option value="">Selecciona un amigo</option>
                          {friends
                            .filter(friend => !selectedGroup.users.includes(friend.username))
                            .map(friend => (
                              <option key={friend.id} value={friend.username}>
                                {friend.username}
                              </option>
                            ))}
                        </select>
                        <button
                          className="modal-invite-btn"
                          disabled={!selectedFriend}
                          style={{marginLeft: "0.5rem"}}
                          onClick={() => handleInvite(selectedFriend, selectedGroup.id)}
                        >
                          Invitar
                        </button>
                      </div>
                    )}
                  </>
                )}

                <div className="modal-buttons">
                  <button className="modal-close-btn" onClick={() => setSelectedGroup(null)}>Cerrar</button>
                  <button
                    className="neon-button-danger"
                    onClick={() =>
                      handleLeaveOrDelete(
                        selectedGroup.id,
                        selectedGroup.creatorUsername === currentUser.username
                      )
                    }
                  >
                    {selectedGroup.creatorUsername === currentUser.username
                      ? "Borrar grupo"
                      : "Abandonar"}
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default MyGroups;
