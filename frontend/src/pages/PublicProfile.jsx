import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../static/resources/css/MyProfile.css";

function PublicProfile() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [publicUser, setPublicUser] = useState(null);
    const [status, setStatus] = useState("loading");
    const apiUrl = import.meta.env.VITE_API_URL;
    const token = localStorage.getItem("jwt");

    useEffect(() => {
        const fetchPublicUser = async () => {
            try {
                const res = await fetch(`${apiUrl}/api/users/public/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });

                if (res.status === 500) {
                    setStatus("notfound");
                    return;
                }

                if (res.status === 409) {
                    navigate("/my-profile");
                    return;
                }

                const data = await res.json();
                setPublicUser(data);
                setStatus("ok");
            } catch (error) {
                console.error("Error al cargar perfil público", error);
                setStatus("notfound");
            }
        };

        fetchPublicUser();
    }, [id, navigate]);

    const handleAddFriend = async () => {
        try {
            const res = await fetch(`${apiUrl}/api/invitations/create`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    receiverUsername: publicUser.username,
                    groupInvitation: false,
                }),
            });

            if (!res.ok) {
                const errorData = await res.json();
                throw new Error(errorData.error || "Error al enviar invitación");
            }

            alert(" Invitación enviada");
        } catch (err) {
            alert(`${err.message}`);
            console.error(err);
        }
    };


    return (
        <div className="profile-container">
            {status === "loading" && (
                <div className="profile-box" style={{ justifyContent: "center", textAlign: "center" }}>
                    <h2>Cargando perfil...</h2>
                </div>
            )}

            {status === "notfound" && (
                <div className="profile-box" style={{ justifyContent: "center", textAlign: "center" }}>
                    <h2 style={{ color: "#ff4d4d" }}> Usuario no encontrado</h2>
                </div>
            )}

            {status === "ok" && publicUser && (
                <div className="profile-box">
                    <div className="profile-left">
                        <img
                            src={`http://localhost:8080${publicUser.profilePictureUrl || "/resources/images/defecto.png"}`}
                            alt="Foto de perfil"
                            className="profile-pic"
                        />
                        {publicUser.friend ? (
                            <span className="profile-btn" style={{ cursor: "default" }}>
                                Sois amigos
                            </span>
                        ) : (
                            <button className="profile-btn" onClick={handleAddFriend}>
                                Enviar solicitud de amistad
                            </button>
                        )}
                    </div>

                    <div className="profile-right">
                        <div className="username-display">{publicUser.username}</div>
                        <span className="profile-description">
                            <strong>Descripción:</strong> {publicUser.description || "Sin descripción"}
                        </span>
                        <span>
                            <strong>Juego favorito:</strong> {publicUser.favGame || "No especificado"}
                        </span>
                    </div>
                </div>
            )}
        </div>
    );
}

export default PublicProfile;
