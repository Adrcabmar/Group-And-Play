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
INSERT IGNORE INTO users (id, username, role, email, first_name, last_name, telephone, password, profile_picture_url, fav_game_id) VALUES 
(1, 'alice123', 'USER', 'alice@example.com', 'Alice', 'Johnson', '123456789', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 1),
(2, 'bob456', 'USER','bob@example.com', 'Bob', 'Smith', '987654321', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 1),
(3, 'charlie789', 'USER','charlie@example.com', 'Charlie', 'Brown', '567123890', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', null),
(4, 'diana001', 'USER','diana@example.com', 'Diana', 'Prince', '654321987', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 2),
(5, 'edward_dev', 'USER','edward@example.com', 'Edward', 'Snowden', '321789654', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', 3),
(6, 'admin', 'ADMIN','admin@example.com', 'Admin', 'Admin', '322789654', '$2a$10$N61mfnDDy4RAclKH/H2VTePP3gU87OGFy0heWsC6ulVLpXMoc47LC', '/resources/images/defecto.png', null);

-- GRUPOS
INSERT IGNORE INTO groups (id, status, creation_date, comunication, description, platform, usergame, game_id, creator_id) VALUES 
(1, 'OPEN', NOW(), 'DISCORD', 'Buscamos ADC para rankeds', 'PC', 'aliciaSteam23', 1, 1),  
(2, 'CLOSED', NOW(), 'VOICE_CHAT', 'Equipo competitivo de CS2', 'PC', 'bobiValve', 2, 2),  
(3, 'OPEN', NOW(), 'NO_COMMUNICATION', 'Servidor de Minecraft', 'PLAYSTATION', 'XxCharlie777xX', 4, 3);

-- RELACIONES USER-GROUP
INSERT IGNORE INTO users_groups (user_id, group_id) VALUES 
(1, 1), (2, 1),
(2, 2), (3, 2), (4, 2), 
(3, 3), (5, 3), (1, 3);
