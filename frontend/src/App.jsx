// eslint-disable-next-line no-unused-vars
import React from "react";
import { BrowserRouter as Router, Route, Routes, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import MyNavbar from "./components/Navbar";
import CrearGrupo from "./pages/CrearGrupo";
import MyGroups from "./pages/MyGroups";
import MyProfile from "./pages/MyProfile";
import "./App.css"; 

import { UserProvider, useUser } from "./components/UserContext";

function AppContent() {
  const { user, setUser } = useUser();

  return (
    <div className="app-container"> 
      <div className="neon-static-bg" />
      <Router>
        <div className="navbar"><MyNavbar /></div>
        <div className="content">
          <Routes>
            <Route path="/user" />
            <Route path="/" element={user ? <Home user={user} /> : <Navigate to="/login" />} />
            <Route path="/login" element={<Login setUser={setUser} />} />
            <Route path="/register" element={<Register />} />
            <Route path="/create-group" element={<CrearGrupo />} />
            <Route path="/my-groups" element={<MyGroups />} />
            <Route path="/my-profile" element={<MyProfile />} />
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
