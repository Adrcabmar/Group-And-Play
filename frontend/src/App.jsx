// eslint-disable-next-line no-unused-vars
import React from "react";
import { BrowserRouter as Router, Route, Routes, Navigate } from "react-router-dom";

//Usuarios
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import MyNavbar from "./components/Navbar";
import CrearGrupo from "./pages/CrearGrupo";
import MyGroups from "./pages/MyGroups";
import MyProfile from "./pages/MyProfile";
import Friends from "./pages/Friends";

// Admin
import AdminHome from "./pages/admin/AdminHome";
import AdminUsers from "./pages/admin/AdminUsers";
import AdminGroups from "./pages/admin/AdminGroups";
import AdminGames from "./pages/admin/AdminGames";


import "./App.css"; 
import "./static/resources/css/Navbar.css";

import { UserProvider, useUser } from "./components/UserContext";

function AppContent() {
  const { user, setUser } = useUser();

  function getInitialRoute() {
    if (!user) return "/login";
    if (user.role && user.role.includes("ADMIN")) {
      return "/admin";
    }
    return "/home";
  }

  return (
    <div className="app-container"> 
      <div className="neon-static-bg" />
      <Router>
        <div className="navbar-wrapper"><MyNavbar /></div>
        <div className="content">
          <Routes>
            <Route path="/" element={<Navigate to={getInitialRoute()} />} />
            
            {/* Usuarios */}
            <Route path="/login" element={<Login setUser={setUser} />} />
            <Route path="/register" element={<Register />} />
            <Route path="/home" element={<Home user={user} />} />
            <Route path="/create-group" element={<CrearGrupo />} />
            <Route path="/my-groups" element={<MyGroups />} />
            <Route path="/my-profile" element={<MyProfile />} />
            <Route path="/friends" element={<Friends />} />


            {/* Admin */}
            <Route path="/admin" element={<AdminHome />} />
            <Route path="/admin/users" element={<AdminUsers />} />  
            <Route path="/admin/groups" element={<AdminGroups />} /> 
            <Route path="/admin/games" element={<AdminGames />} />   
  
          </Routes>
        </div>
      </Router>
    </div>
  );
}

function App() {
  return (
    <UserProvider>
      <AppContent />
    </UserProvider>
  );
}

export default App;
