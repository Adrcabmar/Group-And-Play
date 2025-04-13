/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import React, { useEffect, useState } from "react";

const MyGroups = () => {
    const token = localStorage.getItem("jwt");
    const [groups, setGroups] = useState([]);
    const currentUser = JSON.parse(localStorage.getItem("user"));
    useEffect(() => {
        fetchGroups();
    }, []);
    
    const fetchGroups = async () => {
        const apiUrl = import.meta.env.VITE_API_URL;
        if (!token) {
          console.error("⚠ No hay token almacenado en localStorage");
          return;
        }
    
        try {
          const response = await fetch(`${apiUrl}/api/groups/my-groups`, {
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
          setGroups(data);
        } catch (error) {
          console.error("⚠ Error al obtener los grupos:", error);
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
          headers: {
            Authorization: `Bearer ${token}`,
          },
        })
          .then((res) => {
            if (res.ok) {
              setGroups((prev) => prev.filter((g) => g.id !== groupId));
            } else {
              console.error("Error al abandonar/eliminar grupo");
            }
          })
          .catch((err) => console.error("Error:", err));
      };

      return (
        <div style={{ padding: "2rem", backgroundColor: "#b3e0f2" }}>
          <h1 style={{ textAlign: "center" }}>My Groups</h1>
          <div style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
            gap: "1.5rem",
            marginTop: "2rem",
          }}>
            {groups.map((group) => {
              const isCreator = group.creatorUsername === currentUser.username;
              return (
                <div key={group.id} style={{
                  border: "2px solid black",
                  padding: "1rem",
                  position: "relative",
                  backgroundColor: "#f0f8ff",
                  borderRadius: "10px",
                  boxShadow: "2px 2px 10px rgba(0,0,0,0.1)",
                }}>
                  <h2>{group.gameName} - {group.status}</h2>
                  <p><em>{group.description || "Sin descripción"}</em></p>
                  <p>Jugadores: {group.users.length} / {group.maxPlayers}</p>
                  <p>
                    Creador: <strong>{group.creatorUsername}</strong>{" "}
                    {isCreator && <span style={{ color: "#e67e22" }}> (Tú)</span>}
                  </p>
                  <p>Comunicación: {group.communication}</p>
                  <button
                    onClick={() => handleLeaveOrDelete(group.id, isCreator)}
                    style={{
                      position: "absolute",
                      bottom: "1rem",
                      right: "1rem",
                      padding: "0.5rem 1rem",
                      backgroundColor: isCreator ? "#e74c3c" : "#2980b9",
                      color: "white",
                      border: "none",
                      borderRadius: "5px",
                      cursor: "pointer"
                    }}
                  >
                    {isCreator ? "Borrar grupo" : "Abandonar"}
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      );
    };
    

export default MyGroups;