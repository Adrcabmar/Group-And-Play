import { useEffect, useState } from "react";
import "../static/resources/css/Invitations.css";

function Invitations() {
    const [invitations, setInvitations] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const token = localStorage.getItem("jwt");
    const apiUrl = import.meta.env.VITE_API_URL;

    useEffect(() => {
        fetchInvitations();
    }, [page]);

    const fetchInvitations = async () => {
        try {
            const res = await fetch(
                `${apiUrl}/api/invitations/group-invitations?page=${page}&size=5`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            const data = await res.json();
            setInvitations(data.content);
            setTotalPages(data.totalPages);
        } catch (err) {
            console.error("Error fetching invitations", err);
        }
    };

    const handleAccept = async (id) => {
        try {
            await fetch(`${apiUrl}/api/invitations/accept/${id}`, {
                method: "POST",
                headers: { Authorization: `Bearer ${token}` },
            });
            fetchInvitations();
        } catch (err) {
            console.error("Error accepting invitation", err);
        }
    };

    const handleReject = async (id) => {
        try {
            await fetch(`${apiUrl}/api/invitations/reject/${id}`, {
                method: "POST",
                headers: { Authorization: `Bearer ${token}` },
            });
            fetchInvitations();
        } catch (err) {
            console.error("Error rejecting invitation", err);
        }
    };

    return (
        <div className="invitations-wrapper">
            <div className="invitations-container">
                <h2 className="neon-title">Invitaciones a grupos</h2>

                {invitations.length === 0 ? (
                    <p className="no-invitations">No tienes invitaciones pendientes.</p>
                ) : (
                    <>
                        {invitations.map((inv) => (
                            <div key={inv.id} className="invitation-card">
                                <p className="invitation-date">
                                    {new Date(inv.date).toLocaleString("es-ES", {
                                        day: "2-digit",
                                        month: "2-digit",
                                        year: "numeric",
                                        hour: "2-digit",
                                        minute: "2-digit"
                                    })}
                                </p>
                                <p>
                                    <strong>{inv.senderUsername}</strong> te ha invitado a un grupo de {inv.gameName}{" "}
                                    <strong>{inv.groupName}</strong>
                                </p>
                                <div className="invitation-actions">
                                    <button onClick={() => handleAccept(inv.id)}>Aceptar</button>
                                    <button onClick={() => handleReject(inv.id)}>Rechazar</button>
                                </div>
                            </div>
                        ))}

                        <div className="pagination">
                            <button disabled={page === 0} onClick={() => setPage(page - 1)}>
                                Anterior
                            </button>
                            <span className="pagination-text">
                                Página {page + 1} de {totalPages}
                            </span>
                            <button
                                disabled={page + 1 >= totalPages}
                                onClick={() => setPage(page + 1)}
                            >
                                Siguiente
                            </button>
                        </div>
                    </>
                )}


            </div>
        </div>
    );
}

export default Invitations;
