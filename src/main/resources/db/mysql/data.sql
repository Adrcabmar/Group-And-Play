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
INSERT IGNORE INTO users (id, username, role, email, first_name, last_name, description, password, profile_picture_url, fav_game_id, discord_name) VALUES 
(1, 'alice123', 'USER', 'alice@example.com', 'Alice', 'Johnson', 'Apasionada de las partidas casuales, no me gusta competir pero si divertirme', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 1, null),
(2, 'bob456', 'USER','bob@example.com', 'Bob', 'Smith', 'No se que poner en la descripcion pero me encanta el Lol, soy Diamante y busco gente para rankear', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 1, null),
(3, 'charlie789', 'USER','charlie@example.com', 'Charlie', 'Brown', null, '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', null, null),
(4, 'diana001', 'USER','diana@example.com', 'Diana', 'Prince', 'Klk algn para Skywars?', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 2, null),
(5, 'edward_dev', 'USER','edward@example.com', 'Edward', 'Snowden', null, '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 3, null),
(6, 'admin', 'ADMIN','admin@example.com', 'Admin', 'Admin', null, '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', null, null);

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

--CHATS
INSERT IGNORE INTO chat (id, game_id) VALUES 
(1, 1),
(2, 2),
(3, 3),
(4, 4), 
(5, 5);

--MENSAJES
INSERT IGNORE INTO message (id, chat_id, sender_id, content, date) VALUES 
-- Chat del LoL
(1, 1, 1, 'Hola, alguien para rankear?', '2025-05-16 10:00:00'),
(2, 1, 2, 'Yo me apunto, main jungla!', '2025-05-16 10:01:00'),

-- Chat del CS2
(3, 2, 2, 'Estamos buscando un AWP para el equipo.', '2025-05-16 11:00:00'),
(4, 2, 3, 'Yo juego AWP, soy nivel alto.', '2025-05-16 11:02:00'),

-- Chat del Valo
(5, 3, 4, 'Sage o Reyna?', '2025-05-16 12:00:00'),
(6, 3, 5, 'Reyna siempre bb', '2025-05-16 12:01:00'),

-- Chat del Mine
(7, 4, 3, 'Estoy construyendo una granja de hierro.', '2025-05-16 13:00:00'),
(8, 4, 1, 'Pásame las coordenadas!', '2025-05-16 13:02:00'),

-- Chat del Overwatch
(9, 5, 5, '¿Quién se anima a unas partidas rápidas?', '2025-05-16 14:00:00'),
(10, 5, 4, 'Voy contigo, juego Sojourn.', '2025-05-16 14:01:00');
