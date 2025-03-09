// eslint-disable-next-line no-unused-vars
import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Route, Routes, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import MyNavbar from "./components/Navbar";
import "./App.css"; 
import { useCurrentUser } from "./hooks/useCurrentUser";
import CrearGrupo from "./pages/CrearGrupo";


function App() {
  const {currentUser, loading, setCurrentUser} = useCurrentUser(null);

  if (loading) {
    return <div>Loading...</div>; // Muestra algo mientras se carga el usuario
  }

  return (
    <div className="app-container"> 
      <Router>
        <div className="navbar"><MyNavbar /></div>
        <div className="content">
          <Routes>
            <Route path="/user"  />
            <Route path="/" element={currentUser ? <Home user={currentUser} /> : <Navigate to="/login" />} />
            <Route path="/login" element={<Login setUser={setCurrentUser} />} />
            <Route path="/register" element={<Register />} />
            <Route path="/create-group" element={<CrearGrupo />} />
          </Routes>
        </div>
      </Router>
    </div>
  );
}

export default App;
