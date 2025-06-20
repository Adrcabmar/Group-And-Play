/* eslint-disable no-unused-vars */
import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";

const Register = () => {
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    password: "",
  });
  const apiUrl = import.meta.env.VITE_API_URL;
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await axios.post(`${apiUrl}/api/users/auth/register`, {
        firstName: form.firstName,
        lastName: form.lastName,
        username: form.username,
        email: form.email,
        password: form.password,
        role: "USER"      
      });

      if (response.data.error) {
        setError("Error: " + response.data.error);
        return;
      }

      navigate("/login"); 
    } catch (error) {
      console.error("Error en el registro:", error.response?.data || error.message);
      setError("Error al registrarse. Inténtalo de nuevo.");
    }
  };

  return (
    <div className="login-container" style={{ marginTop: "2rem" }}>
      <div className="login-box">
        <h2>Registro</h2>
        {error && <p style={{ color: "red" }}>{error}</p>}
        <form onSubmit={handleSubmit}>
          <input type="text" name="firstName" placeholder="Nombre" value={form.firstName} onChange={handleChange} required />
          <input type="text" name="lastName" placeholder="Apellido" value={form.lastName} onChange={handleChange} required />
          <input type="text" name="username" placeholder="Nombre de usuario" value={form.username} onChange={handleChange} required />
          <input type="email" name="email" placeholder="Correo electrónico" value={form.email} onChange={handleChange} required />
          <input type="password" name="password" placeholder="Contraseña" value={form.password} onChange={handleChange} required />
          <button type="submit">Registrarse</button>
        </form>
        <p>¿Ya tienes una cuenta? <Link to="/login">Inicia sesión</Link></p>
      </div>
    </div>
  );
};

export default Register;
