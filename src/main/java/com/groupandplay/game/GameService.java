package com.groupandplay.game;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    public Game findById(Integer gameId) throws IllegalArgumentException {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
    }

    public List<Game> findAllGames() {
        List<Game> games = gameRepository.findAll();
        return games != null ? games : List.of(); 
    }

    public List<String> findAllGamesName() {
        return gameRepository.findAll()
                .stream()
                .map(Game::getName)
                .toList();
    }
}
