// eslint-disable-next-line no-unused-vars
import React, { useState } from 'react';
import { Navbar, NavbarBrand, Nav, NavItem, NavLink, Dropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap';
import { useNavigate } from 'react-router-dom';
import logo from "../static/resources/images/Logo.png";
import { useUser } from "../components/UserContext";

const MyNavbar = () => {
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const { user: currentUser } = useUser();

  if (!currentUser) return null;

  
  const isAdmin = currentUser.role === "ADMIN";


  const toggleDropdown = () => setDropdownOpen(!dropdownOpen);

  const handleLogout = () => {
    localStorage.removeItem("jwt");
    localStorage.removeItem("user");
    window.location.href = "/";
  };

  return (
    <Navbar className="navbar-container">
      <div className="navbar-left">
        <NavbarBrand href="/">
          <img src={logo} alt="logo" className="navbar-logo" />
        </NavbarBrand>
      </div>

      <div className="navbar-center">
        <Nav>
          {isAdmin ? (
            <>
              <NavItem>
                <NavLink href="/admin/groups" className="nav-link navbar-button">Grupos</NavLink>
              </NavItem>
              <NavItem>
                <NavLink href="/admin/users" className="nav-link navbar-button">Usuarios</NavLink>
              </NavItem>
              <NavItem>
                <NavLink href="/admin/games" className="nav-link navbar-button">Juegos</NavLink>
              </NavItem>
            </>
          ) : (
            <>
              <NavItem>
                <NavLink href="/" className="nav-link navbar-button">Buscar grupo</NavLink>
              </NavItem>
              <NavItem>
                <NavLink href="/my-groups" className="nav-link navbar-button">Mis grupos</NavLink>
              </NavItem>
              <NavItem>
                <NavLink href="/create-group" className="nav-link navbar-button">Crear grupo</NavLink>
              </NavItem>
              <NavItem>
                <NavLink href="/invitations" className="nav-link navbar-button"> Invitaciones</NavLink>
              </NavItem>
            </>
          )}
        </Nav>
      </div>

      <div className="navbar-right">
        <Dropdown isOpen={dropdownOpen} toggle={toggleDropdown} className="user-dropdown" direction="down">
          <DropdownToggle tag="span" style={{ cursor: "pointer" }}>
            <span className="user-name" style={{ marginRight: "10px" }}>{currentUser?.username}</span>
            <img
              src={`http://localhost:8080${currentUser?.profilePictureUrl || "/resources/images/defecto.png"}`}
              alt="Usuario"
              className="user-avatar"
            />
          </DropdownToggle>
          <DropdownMenu
            style={{
              right: "0", left: "auto",
              transform: "translateX(-10%) translateY(40px)",
              backgroundColor: "#B3E5FC"
            }}
          >
            <DropdownItem
              style={{ color: "black", backgroundColor: "transparent" }}
              onMouseEnter={(e) => e.target.style.backgroundColor = "#4FC3F7"}
              onMouseLeave={(e) => e.target.style.backgroundColor = "transparent"}
              onClick={() => navigate("/my-profile")}
            >
              Mi perfil
            </DropdownItem>

            <DropdownItem
              style={{ color: "black", backgroundColor: "transparent" }}
              onMouseEnter={(e) => e.target.style.backgroundColor = "#4FC3F7"}
              onMouseLeave={(e) => e.target.style.backgroundColor = "transparent"}
              onClick={() => navigate("/friends")}
            >
              Amigos
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
      </div>
    </Navbar>
  );
};

export default MyNavbar;
