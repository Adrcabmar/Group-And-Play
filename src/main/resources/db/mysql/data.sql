INSERT IGNORE INTO users (id, username, email, first_name, last_name, telephone, password) VALUES 
(1, 'alice123', 'alice@example.com', 'Alice', 'Johnson', '123456789', '$2a$10$gyqEuh5bpJVhwuN44YwkjeaR6/1u2KqFTGhjYxWtH27Dmka569AmK'),
(2, 'bob456', 'bob@example.com', 'Bob', 'Smith', '987654321', '$2a$10$gyqEuh5bpJVhwuN44YwkjeaR6/1u2KqFTGhjYxWtH27Dmka569AmK'),
(3, 'charlie789', 'charlie@example.com', 'Charlie', 'Brown', '567123890', '$2a$10$gyqEuh5bpJVhwuN44YwkjeaR6/1u2KqFTGhjYxWtH27Dmka569AmK'),
(4, 'diana001', 'diana@example.com', 'Diana', 'Prince', '654321987', '$2a$10$gyqEuh5bpJVhwuN44YwkjeaR6/1u2KqFTGhjYxWtH27Dmka569AmK'),
(5, 'edward_dev', 'edward@example.com', 'Edward', 'Snowden', '321789654', '$2a$10$gyqEuh5bpJVhwuN44YwkjeaR6/1u2KqFTGhjYxWtH27Dmka569AmK');

INSERT INTO games (id, name, max_players) VALUES 
(1, 'League of Legends', 5),
(2, 'Counter-Strike 2', 10),
(3, 'Valorant', 5),
(4, 'Minecraft', 100),
(5, 'Overwatch 2', 6);

INSERT INTO groups (id, status, creation_date, comunication, description, game_id, creator_id) VALUES 
(1, 'OPEN', NOW(), 'DISCORD', 'Buscamos ADC para rankeds', 1, 1),  
(2, 'CLOSED', NOW(), 'VOICE_CHAT', 'Equipo competitivo de CS2', 2, 2),  
(3, 'OPEN', NOW(), 'NO_COMMUNICATION', 'Servidor de Minecraft', 4, 3);  

INSERT INTO users_groups (user_id, group_id) VALUES 
(1, 1), (2, 1),
(3, 2), (4, 2), 
(5, 3), (1, 3); 
