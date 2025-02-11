/* eslint-disable no-unused-vars */
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import Helmet from "react-helmet";

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
    localStorage.removeItem("user"); 
    navigate("/login");
  };

  return (
    <div className="d-flex justify-content-center align-items-center vh-100"> 
      <div className="text-center">
        <Helmet>
          <title>Bienvenido {currentUser?.username || "Desconocido"}</title>
          <link
            rel="stylesheet"
            href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css"
          />
        </Helmet>

        <h2 className="mb-3">Hola usuario: {currentUser?.username || "Desconocido"}</h2>
      </div>
    </div>
  );
}

export default Home;
