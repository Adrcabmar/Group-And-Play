/* eslint-disable no-unused-vars */
import React, { useState } from 'react';
import { Navbar, NavbarBrand, Nav, NavItem, NavLink, Button, Dropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap';
import { useNavigate } from 'react-router-dom';
import logo from "../static/resources/images/Logo.png"; 
import userIcon from "../static/resources/images/user.png"; 

const MyNavbar = () => {
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);


  const currentUser = JSON.parse(localStorage.getItem("user"));
  
  if (!currentUser) {
    return null; 
  }
  const toggleDropdown = () => {
    setDropdownOpen(!dropdownOpen);
  };
  const handleLogout = () => {
    localStorage.removeItem("jwt");
    localStorage.removeItem("user");
    window.location.href = "/";
  };

  return (
    <Navbar className="navbar-container">
      <NavbarBrand href="/">
        <img src={logo} alt="logo" className="navbar-logo" />
      </NavbarBrand>

      <Nav className="nav-center">
        <NavItem>
          <NavLink  style={{ backgroundColor: "#B3E5FC", color: "black", border: "none", borderRadius: "7px"}}   href="/my-groups" className="nav-link">
            Mis grupos
          </NavLink>
        </NavItem>
      </Nav>

      <Nav className="nav-right">
        <NavItem className="user-info">
          <Dropdown isOpen={dropdownOpen} toggle={toggleDropdown} className="user-dropdown" direction="down">
            <DropdownToggle tag="span" data-toggle="dropdown" aria-expanded={dropdownOpen} style={{ cursor: "pointer", marginLeft: "10px" }}>
              <span className="user-name">{currentUser?.username}</span>
              <img src={userIcon} alt="Usuario" className="user-avatar" />
            </DropdownToggle>
            <DropdownMenu style={{ right: "0", left: "auto", transform: "translateX(-10%) translateY(40px)", backgroundColor: "#B3E5FC", border: "yes" }}>
              <DropdownItem 
                style={{ color: "black", backgroundColor: "transparent" }} 
                onMouseEnter={(e) => e.target.style.backgroundColor = "#4FC3F7"}
                onMouseLeave={(e) => e.target.style.backgroundColor = "transparent"}
                onClick={() => navigate("/myprofile")}
              >
                Mi perfil
              </DropdownItem>
              <DropdownItem 
                style={{ 
                  backgroundColor: "#81D4FA", 
                  color: "black", 
                  border: "none", 
                  width: "100%", 
                  textAlign: "center" 
                }}
                onClick={handleLogout}
                onMouseEnter={(e) => e.target.style.backgroundColor = "#4FC3F7"}
                onMouseLeave={(e) => e.target.style.backgroundColor = "#81D4FA"}
              >
                Cerrar sesión
              </DropdownItem>

            </DropdownMenu>
          </Dropdown>
        </NavItem>
      </Nav>
    </Navbar>
  );
};

export default MyNavbar;
