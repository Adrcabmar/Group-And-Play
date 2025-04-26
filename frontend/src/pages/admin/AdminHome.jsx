import { useUser } from "../../components/UserContext";
import { Navigate, useNavigate } from "react-router-dom";
import "../../static/resources/css/admin/AdminHome.css";

function AdminHome() {
  const { user } = useUser();
  const navigate = useNavigate();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (user.role !== "ADMIN") {
    return <Navigate to="/home" replace />;
  }

  return (
    <div className="admin-home-container">
      <div className="admin-home-main">
        <div className="admin-home-buttons">
          <button className="admin-button" onClick={() => navigate("/admin/groups")}>
            Administrar Grupos
          </button>
          <button className="admin-button" onClick={() => navigate("/admin/users")}>
            Administrar Usuarios
          </button>
          <button className="admin-button" onClick={() => navigate("/admin/games")}>
            Administrar Juegos
          </button>
        </div>
      </div>
    </div>
  );
}

export default AdminHome;
