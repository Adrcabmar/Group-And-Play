import "../static/resources/css/Friends.css";
import { useEffect, useState } from 'react';
import { useAlert } from "../components/AlertContext";

function Friends() {
    const [friends, setFriends] = useState([]);
    const [usernameSearch, setUsernameSearch] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [addUsername, setAddUsername] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const apiUrl = import.meta.env.VITE_API_URL;
    const token = localStorage.getItem("jwt");
    const [requests, setRequests] = useState([]);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [selectedToDelete, setSelectedToDelete] = useState(null);
    const { showAlert } = useAlert();



    useEffect(() => {
        fetchFriends();
        fetchFriendRequests();
    }, [page, usernameSearch]);

    const fetchFriendRequests = async () => {
        try {
            const res = await fetch(`${apiUrl}/api/invitations/friend-invitations`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            if (!res.ok) throw new Error("Error al obtener solicitudes");
            const data = await res.json();
            setRequests(data.content || []);
        } catch (err) {
            console.error("Error al obtener solicitudes", err);
        }
    };

    const handleAccept = async (id) => {
        try {
            const res = await fetch(`${apiUrl}/api/invitations/accept/${id}`, {
                method: "POST",
                headers: { Authorization: `Bearer ${token}` }
            });
            if (!res.ok) throw new Error("Error al aceptar la invitación");
            setRequests((prev) => prev.filter((r) => r.id !== id));
            fetchFriends();
        } catch (err) {
            console.error("Error aceptando la invitación", err);
        }
    };

    const handleReject = async (id) => {
        try {
            const res = await fetch(`${apiUrl}/api/invitations/reject/${id}`, {
                method: "POST",
                headers: { Authorization: `Bearer ${token}` }
            });
            if (!res.ok) throw new Error("Error al rechazar la invitación");
            setRequests((prev) => prev.filter((r) => r.id !== id));
        } catch (err) {
            console.error("Error rechazando la invitación", err);
        }
    };

    const fetchFriends = async () => {
        try {
            const res = await fetch(`${apiUrl}/api/users/friends/all?page=${page}&size=5&username=${encodeURIComponent(usernameSearch)}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            if (!res.ok) throw new Error("Error al obtener amigos");
            const data = await res.json();
            setFriends(data.content);
            setTotalPages(data.totalPages);
        } catch (err) {
            console.error("Error fetching friends", err);
        }
    };

    const handleAddFriend = async () => {
        try {
            const res = await fetch(`${apiUrl}/api/invitations/create`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    receiverUsername: addUsername,
                    groupInvitation: false
                })
            });
            if (!res.ok) throw new Error("Error al enviar invitación");
            showAlert("Invitación enviada");
            setShowModal(false);
            setAddUsername('');
        } catch (err) {
            showAlert("Error al enviar la invitación");
            console.error(err);
        }
    };


    const handleDeleteFriend = async (username) => {
        try {
            const res = await fetch(`${apiUrl}/api/users/friends/${username}`, {
                method: "DELETE",
                headers: {
                    Authorization: `Bearer ${token}`,
                }
            });
            if (!res.ok) throw new Error("Error al enviar invitación");
            showAlert("Amigo " + username + " eliminado");
            fetchFriends();
            setShowDeleteModal(false);
            setSelectedToDelete(null);
        } catch (err) {
            showAlert("Error al borrar el amigo");
            console.error(err);
        }
    };

    return (
        <div className="friends-layout">
            <div className="friends-container">
                <div className="friends-box">
                    <h2 className="neon-title">Amigos</h2>

                    <div className="friends-header">
                        <input
                            type="text"
                            className="custom-input"
                            placeholder="Buscar por username..."
                            value={usernameSearch}
                            onChange={(e) => setUsernameSearch(e.target.value)}
                        />
                        <button className="neon-button" onClick={() => setShowModal(true)}>
                            Agregar usuario
                        </button>
                    </div>


                    <div className="friends-list">
                        {friends.length === 0 ? (
                            <p className="no-friends">Actualmente no tienes amigos ¡Invita a alguien!</p>
                        ) : (
                            friends.map((friend) => (
                                <div className="friend-card" key={friend.id}>
                                    <div className="friend-info">
                                        <img
                                            src={`${apiUrl}${friend.profilePictureUrl}`}
                                            alt="avatar"
                                            className="friend-avatar"
                                        />
                                        <span className="friend-name">{friend.username}</span>
                                    </div>
                                    <div className="friend-buttons">
                                        <button
                                            className="neon-button-secondary"
                                            onClick={() => window.location.href = `/user/${friend.id}`}
                                        >
                                            Perfil
                                        </button>
                                        <button
                                            className="neon-button-danger"
                                            onClick={() => {
                                                setSelectedToDelete(friend);
                                                setShowDeleteModal(true);
                                            }}
                                        >
                                            Eliminar
                                        </button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>

                    {friends.length > 0 && (
                        <div className="pagination">
                            <button disabled={page === 0} onClick={() => setPage(page - 1)}>
                                Anterior
                            </button>
                            <span className="pagination-text">
                                Página {page + 1} de {totalPages}
                            </span>
                            <button disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>
                                Siguiente
                            </button>
                        </div>
                    )}
                </div>

                {showModal && (
                    <div className="modal">
                        <div className="modal-content">
                            <h3 className="neon-subtitle">Enviar invitación</h3>
                            <input
                                className="custom-input"
                                type="text"
                                placeholder="Username"
                                value={addUsername}
                                onChange={(e) => setAddUsername(e.target.value)}
                            />
                            <div className="modal-buttons">
                                <button className="neon-button" onClick={handleAddFriend}>
                                    Enviar
                                </button>
                                <button
                                    className="neon-button-secondary"
                                    onClick={() => setShowModal(false)}
                                >
                                    Cancelar
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            <div className="friend-requests-box">
                <h3 className="neon-title">Solicitudes</h3>
                {requests.length === 0 ? (
                    <p className="no-friends">No tienes solicitudes.</p>
                ) : (
                    requests.map((req) => (
                        <div key={req.id} className="friend-request-card">
                            <span>{req.senderUsername}</span>
                            <div className="modal-buttons">
                                <button className="neon-button" onClick={() => handleAccept(req.id)}>
                                    Aceptar
                                </button>
                                <button
                                    className="neon-button-secondary"
                                    onClick={() => handleReject(req.id)}
                                >
                                    Rechazar
                                </button>
                            </div>
                        </div>
                    ))
                )}
            </div>

            {showDeleteModal && selectedToDelete && (
                <div className="modal">
                    <div className="modal-content">
                        <h3 className="neon-subtitle">
                            ¿Eliminar a {selectedToDelete.username}?
                        </h3>
                        <p>¿Estás seguro de que quieres eliminar a este amigo?</p>
                        <div className="modal-buttons">
                            <button
                                className="neon-button"
                                onClick={() => {
                                    handleDeleteFriend(selectedToDelete.username);
                                    setShowDeleteModal(false);
                                    setSelectedToDelete(null);
                                }}
                            >
                                Sí, eliminar
                            </button>
                            <button
                                className="neon-button-secondary"
                                onClick={() => {
                                    setShowDeleteModal(false);
                                    setSelectedToDelete(null);
                                }}
                            >
                                Cancelar
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );



}


export default Friends;