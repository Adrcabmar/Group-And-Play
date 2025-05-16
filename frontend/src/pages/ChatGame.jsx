/* eslint-disable no-unused-vars */
import React, { useEffect, useState, useRef } from "react";
import { useParams } from "react-router-dom";
import SockJS from "sockjs-client/dist/sockjs";
import { Client } from "@stomp/stompjs";
import axios from "axios";

const ChatGame = () => {
    const [messages, setMessages] = useState([]);
    const [newMsg, setNewMsg] = useState("");
    const clientRef = useRef(null);
    const { chatId } = useParams();

    const currentUser = JSON.parse(localStorage.getItem("user"));
    const token = localStorage.getItem("jwt");

    useEffect(() => {
        if (!token || !currentUser) return;

        axios
            .get(`http://localhost:8080/api/chat/${chatId}/messages?limit=20`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            .then((res) => setMessages(res.data))
            .catch((err) => console.error("Error al cargar mensajes:", err));
    }, []);

    useEffect(() => {
        if (!token || !currentUser) return;

        const socketUrl = `http://localhost:8080/ws?token=${token}`;
        const client = new Client({
            webSocketFactory: () => new SockJS(socketUrl),
            onConnect: () => {
                console.log("Conectado al WebSocket");
                client.subscribe(`/topic/chat/${chatId}`, (message) => {
                    const body = JSON.parse(message.body);
                    setMessages((prev) => [...prev, body]);
                });
            },
            onWebSocketError: (error) => {
                console.error("WebSocket error", error);
            },
            onStompError: (frame) => {
                console.error("STOMP error:", frame);
            },
        });

        client.activate();
        clientRef.current = client;

        return () => {
            client.deactivate();
        };
    }, []);

    const sendMessage = () => {
        if (!newMsg.trim() || !clientRef.current || !clientRef.current.connected) {
            console.warn("WebSocket no conectado o mensaje vacío");
            return;
        }

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
        <div style={{ padding: "2rem" }}>
            <h2>Chat de prueba (chatId = {chatId})</h2>

            <div
                style={{
                    border: "1px solid #ccc",
                    height: "300px",
                    overflowY: "auto",
                    marginBottom: "1rem",
                    padding: "1rem",
                }}
            >
                {messages.map((msg, i) => (
                    <div
                        key={msg.id || i}
                        style={{
                            textAlign: msg.senderId === currentUser.id ? "right" : "left",
                            marginBottom: "0.5rem",
                        }}
                    >
                        <strong>{msg.senderUsername}:</strong> {msg.content}
                    </div>
                ))}
            </div>

            <input
                type="text"
                value={newMsg}
                onChange={(e) => setNewMsg(e.target.value)}
                placeholder="Escribe un mensaje..."
            />
            <button onClick={sendMessage}>Enviar</button>
        </div>
    );
};

export default ChatGame;
