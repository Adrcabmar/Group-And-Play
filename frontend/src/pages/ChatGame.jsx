/* eslint-disable no-unused-vars */
import React, { useEffect, useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import SockJS from "sockjs-client/dist/sockjs";
import { Client } from "@stomp/stompjs";
import axios from "axios";
import "../static/resources/css/ChatGame.css";

const ChatGame = () => {
    const [messages, setMessages] = useState([]);
    const [newMsg, setNewMsg] = useState("");
    const [game, setGame] = useState(null);

    const clientRef = useRef(null);
    const bottomRef = useRef(null);
    const messagesContainerRef = useRef(null);
    const { chatId } = useParams();
    const apiUrl = import.meta.env.VITE_API_URL;
    const currentUser = JSON.parse(localStorage.getItem("user"));
    const token = localStorage.getItem("jwt");
    const navigate = useNavigate();

    useEffect(() => {
        if (!token || !currentUser) return;

        axios
            .get(`${apiUrl}/api/chat/${chatId}/messages?limit=20`, {
                headers: { Authorization: `Bearer ${token}` },
            })
            .then((res) => {
                setMessages(res.data);

                setTimeout(() => {
                    const container = messagesContainerRef.current;
                    if (container) {
                        container.scrollTop = container.scrollHeight;
                    }
                }, 0);
            })
            .catch((err) => console.error("Error al cargar mensajes:", err));

        fetchGame(chatId);
    }, [chatId]);

    useEffect(() => {
        if (!token || !currentUser) return;

        const socketUrl = `${apiUrl}/ws?token=${token}`;
        const client = new Client({
            webSocketFactory: () => new SockJS(socketUrl),
            onConnect: () => {
                client.subscribe(`/topic/chat/${chatId}`, (message) => {
                    const body = JSON.parse(message.body);
                    setMessages((prev) => [...prev, body]);
                });
            },
            onWebSocketError: (error) => console.error("WebSocket error", error),
            onStompError: (frame) => console.error("STOMP error:", frame),
        });

        client.activate();
        clientRef.current = client;

        return () => {
            client.deactivate();
        };
    }, [chatId]);

    useEffect(() => {
        const container = messagesContainerRef.current;
        const isAtBottom =
            container.scrollHeight - container.scrollTop - container.clientHeight < 100;

        if (isAtBottom) {
            bottomRef.current?.scrollIntoView({ behavior: "smooth" });
        }
    }, [messages]);

    const fetchGame = async (id) => {
        try {
            const res = await fetch(`${apiUrl}/api/games/search/${id}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });
            if (res.ok) {
                const game = await res.json();
                setGame(game);
            } else {
                console.warn("No se pudo obtener el juego");
            }
        } catch (err) {
            console.error(err);
        }
    };

    const sendMessage = () => {
        if (!newMsg.trim() || !clientRef.current?.connected) return;

        const dto = {
            content: newMsg,
            senderId: currentUser.id,
            senderUsername: currentUser.username,
        };

        try {
            clientRef.current.publish({
                destination: `/app/chat/${chatId}`,
                body: JSON.stringify(dto),
            });
            setNewMsg("");
        } catch (err) {
            console.error("Error enviando mensaje:", err);
        }
    };

    return (
        <div className="chat-container">
            <h2 className="chat-title">Chat de {game ? game.name : "..."}</h2>

            <div className="chat-messages" ref={messagesContainerRef}>
                {messages.map((msg, i) => {
                    const isOwn = msg.senderUsername === currentUser.username;
                    return (
                        <div
                            key={msg.id || i}
                            className={`chat-message ${isOwn ? "own-message" : "other-message"}`}
                        >
                            <div
                                className="profile-bubble"
                                onClick={() =>
                                    msg.senderId === currentUser.id
                                        ? navigate("/my-profile")
                                        : navigate(`/user/${msg.senderId}`)
                                }
                                title={`Ver perfil de ${msg.senderUsername}`}
                                style={{ cursor: "pointer" }}
                            >
                                <img
                                    src={`${apiUrl}${msg.senderProfilePictureUrl || "/default-profile.png"}`}
                                    alt="profile"
                                    className="profile-img"
                                />
                            </div>

                            <div className="message-content">
                                {!isOwn && <strong className="sender">{msg.senderUsername}</strong>}
                                {msg.content}
                            </div>
                        </div>
                    );
                })}
                <div ref={bottomRef} />
            </div>

            <div className="chat-input-area">
                <input
                    type="text"
                    className="chat-input"
                    value={newMsg}
                    onChange={(e) => {
                        if (e.target.value.length <= 255) {
                            setNewMsg(e.target.value);
                        }
                    }}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            e.preventDefault(); 
                            sendMessage();
                        }
                    }}
                    placeholder="Escribe un mensaje..."
                />
                <small style={{ color: "#00f2ff", marginTop: "0.5rem" }}>
                    {newMsg.length}/255 caracteres
                </small>
                <button className="chat-send-btn" onClick={sendMessage}>Enviar</button>
            </div>
        </div>
    );
};

export default ChatGame;