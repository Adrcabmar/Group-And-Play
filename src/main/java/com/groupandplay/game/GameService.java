package com.groupandplay.game;

import java.util.List;
import java.util.stream.Collectors;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.groupandplay.dto.GameDTO;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Transactional(readOnly = true)
    public Game findById(Integer gameId) throws IllegalArgumentException {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Game> findAllGames() {
        List<Game> games = gameRepository.findAll();
        return games != null ? games : List.of();
    }

    @Transactional(readOnly = true)
    public List<String> findAllGamesName() {
        return gameRepository.findAll()
                .stream()
                .map(Game::getName)
                .toList();
    }

    @Transactional(readOnly = true)
    public Game findByName(String gameName) throws IllegalArgumentException {
        return gameRepository.findByName(gameName)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<Game> findAllPaginatedGames(Pageable pageable, String gameName) {
        if (gameName != null && !gameName.isBlank()) {
            return gameRepository.findByNameContainingIgnoreCase(gameName, pageable);
        }
        return gameRepository.findAll(pageable);
    }
    

    @Transactional
    public Game createGame(GameDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del juego no puede estar vacío");
        }

        if (dto.getMaxPlayers() < 2 || dto.getMaxPlayers() > 1024) {
            throw new IllegalArgumentException("El número de jugadores debe estar entre 2 y 1024");
        }

        if (dto.getPlatforms() == null || dto.getPlatforms().isEmpty()) {
            throw new IllegalArgumentException("Las plataformas no pueden estar vacías");
        }

        Set<Platform> platforms;
        try {
            platforms = dto.getPlatforms()
                    .stream()
                    .map(String::toUpperCase)
                    .map(Platform::valueOf)
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Una o más plataformas no son válidas");
        }

        Game game = new Game();
        game.setName(dto.getName());
        game.setMaxPlayers(dto.getMaxPlayers());
        game.setPlatforms(platforms);

        return gameRepository.save(game);
    }

    @Transactional
    public Game updateGame(GameDTO dto) {
        Game game = gameRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado con ID: " + dto.getId()));

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del juego no puede estar vacío");
        }

        if (dto.getMaxPlayers() < 2 || dto.getMaxPlayers() > 1024) {
            throw new IllegalArgumentException("El número de jugadores debe estar entre 2 y 1024");
        }

        if (dto.getPlatforms() == null || dto.getPlatforms().isEmpty()) {
            throw new IllegalArgumentException("Las plataformas no pueden estar vacías");
        }

        Set<Platform> platforms;
        try {
            platforms = dto.getPlatforms()
                    .stream()
                    .map(String::toUpperCase)
                    .map(Platform::valueOf)
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Una o más plataformas no son válidas");
        }

        game.setName(dto.getName());
        game.setMaxPlayers(dto.getMaxPlayers());
        game.setPlatforms(platforms);

        return gameRepository.save(game);
    }
}
