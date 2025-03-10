/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import Helmet from "react-helmet";
import "../static/resources/css/Home.css"

// eslint-disable-next-line react/prop-types
function Home({ user }) {
  const navigate = useNavigate();

  const handleCreateGroup = () => {
    navigate("/create-group"); 
  };

  return (
    <div className="home-container">
      <aside className="home-left">
        <p>IZQUIERDA</p>
      </aside>

      <main className="home-main">
        <h2 className="welcome-text">
          Hola usuario: {user?.username || "Desconocido"}
        </h2>
      </main>

      <aside className="home-right">
        <button className="create-group-btn" onClick={handleCreateGroup}>
          Crear Grupo
        </button>
      </aside>
    </div>
  );
}


export default Home;
