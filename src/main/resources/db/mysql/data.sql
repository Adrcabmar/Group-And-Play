-- JUEGOS
INSERT IGNORE INTO games (id, name, max_players) VALUES 
(1, 'League of Legends', 5),
(2, 'Counter-Strike 2', 10),
(3, 'Valorant', 5),
(4, 'Minecraft', 100),
(5, 'Overwatch 2', 6);

-- PLATAFORMAS DISPONIBLES POR JUEGO
INSERT IGNORE INTO game_platforms (game_id, platform) VALUES
(1, 'PC'),
(2, 'PC'),
(3, 'PC'),
(4, 'PC'), (4, 'PLAYSTATION'), (4, 'XBOX'),
(5, 'PC'), (5, 'PLAYSTATION'), (5, 'XBOX');

-- USUARIOS
INSERT IGNORE INTO users (id, username, role, email, first_name, last_name, description, password, profile_picture_url, fav_game_id) VALUES 
(1, 'alice123', 'USER', 'alice@example.com', 'Alice', 'Johnson', 'Apasionada de las partidas casuales, no me gusta competir pero si divertirme', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 1),
(2, 'bob456', 'USER','bob@example.com', 'Bob', 'Smith', 'No se que poner en la descripcion pero me encanta el Lol, soy Diamante y busco gente para rankear', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 1),
(3, 'charlie789', 'USER','charlie@example.com', 'Charlie', 'Brown', null, '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', null),
(4, 'diana001', 'USER','diana@example.com', 'Diana', 'Prince', 'Klk algn para Skywars?', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 2),
(5, 'edward_dev', 'USER','edward@example.com', 'Edward', 'Snowden', null, '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 3),
(6, 'admin', 'ADMIN','admin@example.com', 'Admin', 'Admin', null, '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', null);

-- GRUPOS
INSERT IGNORE INTO groups (id, status, creation_date, comunication, description, platform, usergame, game_id, creator_id) VALUES 
(1, 'OPEN', NOW(), 'DISCORD', 'Buscamos ADC para rankeds', 'PC', 'aliciaSteam23', 1, 1),  
(2, 'CLOSED', NOW(), 'VOICE_CHAT', 'Equipo competitivo de CS2', 'PC', 'bobiValve', 2, 2),  
(3, 'OPEN', NOW(), 'NO_COMMUNICATION', 'Servidor de Minecraft', 'XBOX', 'XxCharlie777xX', 4, 3),
(4, 'ClOSED', NOW(), 'NO_COMMUNICATION', 'Servidor de Minecraft', 'PLAYSTATION', 'CharliPlay', 5, 3),
(5, 'OPEN', NOW(), 'VOICE_CHAT', 'Servidor de Minecraft', 'XBOX', 'XxCharlie777xX', 5, 3),
(6, 'OPEN', NOW(), 'NO_COMMUNICATION', 'Servidor de Minecraft', 'PLAYSTATION', 'CharliPlay', 4, 3);


-- RELACIONES USER-GROUP
INSERT IGNORE INTO users_groups (user_id, group_id) VALUES 
(1, 1), (2, 1),
(2, 2), (3, 2), (4, 2), 
(3, 3), (5, 3), (1, 3),
(3, 4),
(3, 5),
(3, 6);

--INVITACIONES
INSERT IGNORE INTO invitation (id, is_group_invitation, sender_id, receiver_id, group_id, date) VALUES 
(1, false, 1, 3, NULL, '2025-05-08 18:30:00'),
(2, true, 2, 1, 2, '2025-05-08 18:45:00'),
(3, false, 3, 1, NULL, '2025-05-15 12:00:00');


--AMIGOS
INSERT IGNORE INTO user_friends (user_id, friend_id) VALUES 
(1, 2), (2, 1),  -- Alice y Bob
(2, 3), (3, 2);  -- Bob y Charlie

