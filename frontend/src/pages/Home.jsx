/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import "../static/resources/css/Home.css";

function Home({ user }) {
  const navigate = useNavigate();
  const [groups, setGroups] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const pageSize = 5;

  const token = localStorage.getItem("jwt");

  useEffect(() => {
    fetchGroups(page);
  }, [page]);

  const fetchGroups = async (pageNumber) => {
    const apiUrl = import.meta.env.VITE_API_URL;

    if (!token) {
      console.error("⚠ No hay token almacenado en localStorage");
      return;
    }

    try {
      const response = await fetch(`${apiUrl}/api/groups/open?page=${pageNumber}&size=${pageSize}`, {
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

        {groups.length > 0 ? (
          <ul className="group-list">
            {groups.map((group) => (
              <li key={group.id} className="group-item d-flex justify-content-between align-items-center">
                <div style={{ textAlign: "left" }}>
                  <h3>{group.name}</h3>
                  <p>{group.description}</p>
                </div>
                <button
                  className="btn btn-success"
                  onClick={() => handleJoinGroup(group.id)}
                >
                  Unirse
                </button>
              </li>
            ))}
          </ul>
        ) : (
          <p>No hay grupos disponibles.</p>
        )}

        <div className="pagination">
          <button disabled={page === 0} onClick={() => setPage(page - 1)}>
            Anterior
          </button>
          <span>Página {page + 1} de {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>
            Siguiente
          </button>
        </div>
      </main>

      <aside className="home-right">
        <button className="create-group-btn btn btn-primary" onClick={() => navigate("/create-group")}>
          Crear Grupo
        </button>
      </aside>
    </div>
  );
}

export default Home;
