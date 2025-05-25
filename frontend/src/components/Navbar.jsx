/* eslint-disable react-hooks/rules-of-hooks */
// eslint-disable-next-line no-unused-vars
import React, { useState, useEffect } from 'react';
import {
  Navbar, NavbarBrand, Nav, NavItem, NavLink, Dropdown,
  DropdownToggle, DropdownMenu, DropdownItem
} from 'reactstrap';
import { useNavigate } from 'react-router-dom';
import logo from "../static/resources/images/logo.png";
import { useUser } from "../components/UserContext";

const MyNavbar = () => {
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [gamesDropdownOpen, setGamesDropdownOpen] = useState(false);
  const [games, setGames] = useState([]);
  const { user: currentUser } = useUser();
  const apiUrl = import.meta.env.VITE_API_URL;
  const token = localStorage.getItem("jwt");

  const isAdmin = currentUser?.role === "ADMIN";
  const toggleDropdown = () => setDropdownOpen(!dropdownOpen);
  const toggleGamesDropdown = () => setGamesDropdownOpen(!gamesDropdownOpen);

  useEffect(() => {
    if (currentUser) {
      fetchGames();
    }
  }, [currentUser]);

  const fetchGames = async () => {
    try {
      const response = await fetch(`${apiUrl}/api/games/all`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const data = await response.json();
      const options = data.map(game => ({
        id: game.id,
        name: game.name,
      }));
      setGames(options);
    } catch (err) {
      console.error("Error al cargar juegos:", err);
    }
  };

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

      {currentUser && (
        <>
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
                    <NavLink href="/invitations" className="nav-link navbar-button">Invitaciones</NavLink>
                  </NavItem>
                  <Dropdown nav isOpen={gamesDropdownOpen} toggle={toggleGamesDropdown}>
                    <DropdownToggle nav caret className="nav-link navbar-button">
                      Chats
                    </DropdownToggle>
                    <DropdownMenu
                      style={{
                        right: "0",
                        left: "auto",
                        transform: "translateX(0%) translateY(15%)",
                        backgroundColor: "#B3E5FC"
                      }}
                    >
                      {games.map((game) => (
                        <DropdownItem
                          key={game.id}
                          onClick={() => navigate(`/chat/${game.id}`)}
                          style={{ color: "black", backgroundColor: "transparent" }}
                          onMouseEnter={(e) => e.target.style.backgroundColor = "#4FC3F7"}
                          onMouseLeave={(e) => e.target.style.backgroundColor = "transparent"}
                        >
                          {game.name}
                        </DropdownItem>
                      ))}
                    </DropdownMenu>
                  </Dropdown>
                </>
              )}
            </Nav>
          </div>

          <div className="navbar-right">
            <Dropdown isOpen={dropdownOpen} toggle={toggleDropdown} className="user-dropdown" direction="down">
              <DropdownToggle tag="span" style={{ cursor: "pointer" }}>
                <span className="user-name" style={{ marginRight: "10px" }}>{currentUser?.username}</span>
                <img
                  src={`${apiUrl}${currentUser?.profilePictureUrl || "/resources/images/defecto.png"}`}
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
        </>
      )}
    </Navbar>
  );
};

export default MyNavbar;