/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */
import React from 'react';
import { Navbar, NavbarBrand, Nav, NavItem, NavLink, Button } from 'reactstrap';
import { useNavigate } from 'react-router-dom';
import logo from "../static/resources/images/Logo.png"; 
import userIcon from "../static/resources/images/user.png"; 

const MyNavbar = ({ handleLogout }) => {
  const navigate = useNavigate();

  const goToUserProfile = () => {
    navigate("/user"); // Redirige a la página del usuario
  };

  return (
    <Navbar className="navbar-container">
      <NavbarBrand href="/">
        <img src={logo} alt="logo" className="navbar-logo" />
      </NavbarBrand>

      <Nav className="nav-center">
        <NavItem>
          <NavLink  style={{ backgroundColor: "#B3E5FC", color: "black", border: "none", borderRadius: "7px"}}   href="/groups" className="nav-link">
            Mis grupos
          </NavLink>
        </NavItem>
      </Nav>

      <Nav className="nav-right">
        <NavItem className="user-info">
          <Button 
            style={{ backgroundColor: "#B3E5FC", color: "black", border: "none" }}  
            onClick={handleLogout}
          >
            Cerrar sesión
          </Button>
          <img 
            src={userIcon} 
            alt="Usuario" 
            className="user-avatar" 
            onClick={goToUserProfile} 
            style={{ cursor: "pointer" }}
          />
        </NavItem>
      </Nav>
    </Navbar>
  );
};

export default MyNavbar;
