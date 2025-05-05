import "../static/resources/css/Friends.css";
import  { useEffect, useState } from 'react';
import axios from 'axios';

function Friends() {
    const [friends, setFriends] = useState([]);
    const [usernameSearch, setUsernameSearch] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [addUsername, setAddUsername] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const apiUrl = import.meta.env.VITE_API_URL;
    const token = localStorage.getItem("jwt");


    useEffect(() => {
        fetchFriends();
    }, [page, usernameSearch]);

    const fetchFriends = async () => {
        try {
            const res = await axios.get(`${apiUrl}/api/users/friends/all`, {
                headers: {
                    Authorization: `Bearer ${token}`
                  },
                params: {
                    page: page,
                    size: 5,
                    username: usernameSearch
                }
            });
            setFriends(res.data.content);
            setTotalPages(res.data.totalPages);
        } catch (err) {
            console.error("Error fetching friends", err);
        }
    };

    const handleAddFriend = async () => {
        try {
            await axios.post(
                `${apiUrl}/api/invitations/create`,
                {
                    receiverUsername: addUsername,
                    groupInvitation: false
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );
            alert("Invitación enviada");
            setShowModal(false);
            setAddUsername('');
        } catch (err) {
            alert(err.response?.data.error || "Error al enviar la invitación");
        }
    };

    return (
        <div className="friends-container">
            <div className="friends-box">
                <div className="friends-header">
                    <input
                        type="text"
                        className="custom-input"
                        placeholder="Buscar por username..."
                        value={usernameSearch}
                        onChange={(e) => setUsernameSearch(e.target.value)}
                    />
                    <button className="neon-button" onClick={() => setShowModal(true)}>Agregar usuario</button>
                </div>

                <h2 className="neon-title">Amigos</h2>

                <div className="friends-list">
                    {friends.length === 0 ? (
                        <p className="no-friends">No se encontraron amigos.</p>
                    ) : (
                        friends.map(friend => (
                            <div className="friend-card" key={friend.id}>
                                <img src={`${apiUrl}${friend.profilePictureUrl}`} alt="avatar" className="friend-avatar" />
                                <span className="friend-name">{friend.username}</span>
                            </div>
                        ))
                    )}
                </div>

                <div className="pagination">
                    <button disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</button>
                    <span className="pagination-text">Página {page + 1} de {totalPages}</span>
                    <button disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Siguiente</button>
                </div>
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
                            <button className="neon-button" onClick={handleAddFriend}>Enviar</button>
                            <button className="neon-button-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}


export default Friends;
