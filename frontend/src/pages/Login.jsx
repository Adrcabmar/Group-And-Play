/* eslint-disable no-unused-vars */
/* eslint-disable react/prop-types */
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "../static/resources/css/Login.css";
import { getCurrentUser } from "../utils/api";

function Login({ setUser }) {
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const apiUrl = import.meta.env.VITE_API_URL;

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(`${apiUrl}/api/users/auth/login` , form);
      const token = response.data.token;
      const data = await getCurrentUser({ token });
      window.localStorage.setItem("user", JSON.stringify(data.user));
      window.localStorage.setItem("jwt", response.data.token);
      setUser(data.user);
      navigate("/");
    } catch (err) {
      setError("Credenciales incorrectas");
    }
  };

  return (
    <div className="login-container"
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "80vh",
      }}>
      <div className="login-box">
        <h2>Inicio de sesión</h2>
        {error && <p style={{ color: "red" }}>{error}</p>}
        <form onSubmit={handleSubmit}>
          <input type="text" name="username" placeholder="Nombre de usuario" onChange={handleChange} required />
          <input type="password" name="password" placeholder="Contraseña" onChange={handleChange} required />
          <button type="submit">Iniciar sesión</button>
        </form>
        <p>¿No tienes una cuenta? <a href="/register">Regístrate</a></p>
      </div>
    </div>
  );
}

export default Login;
