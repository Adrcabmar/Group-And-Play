/* eslint-disable no-unused-vars */
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

// eslint-disable-next-line react/prop-types
function Home({ user }) {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState(user);

  useEffect(() => {
    if (!user) {
      const storedUser = localStorage.getItem("user");
      if (storedUser) {
        setCurrentUser(JSON.parse(storedUser));
      }
    }
  }, [user]);

  const handleLogout = () => {
    localStorage.removeItem("user"); // Eliminar usuario del localStorage
    navigate("/login");
  };

  return (
    <div>
      <h2>Hola usuario: {currentUser?.username || "Desconocido"}</h2>
      <button onClick={handleLogout}>Cerrar sesión</button>
    </div>
  );
}

export default Home;
