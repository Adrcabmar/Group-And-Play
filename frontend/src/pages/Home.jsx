/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import React from "react";
import { useNavigate } from "react-router-dom";

function Home({ user }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    navigate("/login");
  };

  return (
    <div>
      <h2>Hola usuario: {user?.username}</h2>
      <button onClick={handleLogout}>Cerrar sesión</button>
    </div>
  );
}

export default Home;
