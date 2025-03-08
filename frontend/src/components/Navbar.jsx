/* eslint-disable no-unsafe-optional-chaining */
/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */
import React, { useState } from 'react';
import { Navbar, NavbarBrand, Nav, NavItem, NavLink, Button } from 'reactstrap';
import { useNavigate } from 'react-router-dom';
import logo from "../static/resources/images/Logo.png"; 
import userIcon from "../static/resources/images/user.png"; 

const MyNavbar = () => {
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);

  const currentUser = JSON.parse(localStorage.getItem("user")); // Obtener el usuario del localStorage
  
  if (!currentUser) {
    return null; 
  }
  const toggleDropdown = () => {
    setIsOpen(!isOpen);
  };
  const goToUserProfile = () => {
    navigate("/user"); 
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
          <span className="user-name">{(currentUser?.username)}</span>
          <img 
            src={userIcon} 
            alt="Usuario" 
            className="user-avatar" 
            onClick={goToUserProfile} 
            style={{ cursor: "pointer", marginLeft: "10px" }} 
          />
          <Button 
            style={{ backgroundColor: "#B3E5FC", color: "black", border: "none", marginLeft: "10px" }}  
            onClick={() => {
              localStorage.removeItem("jwt");
              localStorage.removeItem("user");
              window.location.href = "/";
            }}
          >
            Cerrar sesión
          </Button>
        </NavItem>
      </Nav>
    </Navbar>
  );
};

export default MyNavbar;
