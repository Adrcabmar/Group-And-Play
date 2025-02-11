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
    telephone: "",
    password: "",
  });

  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    console.log("Formulario enviado:", form); 

    try {
      const response = await axios.post("http://localhost:8080/api/users/register", {
        firstName: form.firstName,
        lastName: form.lastName,
        username: form.username,
        email: form.email,
        telephone: Number(form.telephone), 
        password: form.password,
      });

      console.log("Usuario registrado:", response.data);
      navigate("/"); 
    } catch (error) {
      console.error("Error en el registro:", error.response?.data || error.message);
      setError("Error al registrarse. Inténtalo de nuevo.");
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h2>Registro</h2>
        {error && <p style={{ color: "red" }}>{error}</p>}
        <form onSubmit={handleSubmit}>
          <input type="text" name="firstName" placeholder="Nombre" value={form.firstName} onChange={handleChange} required />
          <input type="text" name="lastName" placeholder="Apellido" value={form.lastName} onChange={handleChange} required />
          <input type="text" name="username" placeholder="Usuario" value={form.username} onChange={handleChange} required />
          <input type="email" name="email" placeholder="Correo electrónico" value={form.email} onChange={handleChange} required />
          <input type="tel" name="telephone" placeholder="Teléfono" value={form.telephone} onChange={handleChange} required />
          <input type="password" name="password" placeholder="Contraseña" value={form.password} onChange={handleChange} required />
          <button type="submit">Registrarse</button>
        </form>
        <p>¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link></p>
      </div>
    </div>
  );
};

export default Register;
