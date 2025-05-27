package com.groupandplay.game.unit;

import com.groupandplay.dto.GameDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;
import com.groupandplay.game.GameService;
import com.groupandplay.game.Platform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GameServiceUnitTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    private Game game;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        game = new Game();
        game.setId(1);
        game.setName("FIFA");
        game.setMaxPlayers(22);
        game.setPlatforms(Set.of(Platform.PLAYSTATION, Platform.PC));
    }

    @Test
    @DisplayName("findById - success")
    void findById_success() {
        when(gameRepository.findById(1)).thenReturn(Optional.of(game));

        Game found = gameService.findById(1);

        assertEquals("FIFA", found.getName());
    }

    @Test
    @DisplayName("findById - not found")
    void findById_notFound() {
        when(gameRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> gameService.findById(1));
    }

    @Test
    @DisplayName("findByName - success")
    void findByName_success() {
        when(gameRepository.findByName("FIFA")).thenReturn(Optional.of(game));

        Game found = gameService.findByName("FIFA");

        assertEquals(22, found.getMaxPlayers());
    }

    @Test
    @DisplayName("findAllGamesName - returns names")
    void findAllGamesName_success() {
        when(gameRepository.findAll()).thenReturn(List.of(game));

        List<String> names = gameService.findAllGamesName();

        assertEquals(List.of("FIFA"), names);
    }

    @Test
    @DisplayName("createGame - success")
    void createGame_success() {
        GameDTO dto = new GameDTO();
        dto.setName("Rocket League");
        dto.setMaxPlayers(8);
        dto.setPlatforms(Set.of("PC", "PLAYSTATION"));

        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

        Game created = gameService.createGame(dto);

        assertEquals("Rocket League", created.getName());
        assertTrue(created.getPlatforms().contains(Platform.PC));
    }

    @Test
    @DisplayName("createGame - invalid platform")
    void createGame_invalidPlatform() {
        GameDTO dto = new GameDTO();
        dto.setName("Rocket League");
        dto.setMaxPlayers(8);
        dto.setPlatforms(Set.of("ASGHASG"));

        assertThrows(IllegalArgumentException.class, () -> gameService.createGame(dto));
    }

    @Test
    @DisplayName("updateGame - success")
    void updateGame_success() {
        GameDTO dto = new GameDTO();
        dto.setId(1);
        dto.setName("FIFA 24");
        dto.setMaxPlayers(22);
        dto.setPlatforms(Set.of("PC"));

        when(gameRepository.findById(1)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

        Game updated = gameService.updateGame(dto);

        assertEquals("FIFA 24", updated.getName());
        assertEquals(1, updated.getPlatforms().size());
        assertTrue(updated.getPlatforms().contains(Platform.PC));
    }

    @Test
    @DisplayName("updateGame - not found")
    void updateGame_notFound() {
        GameDTO dto = new GameDTO();
        dto.setId(99);
        dto.setName("New Game");
        dto.setMaxPlayers(4);
        dto.setPlatforms(Set.of("PC"));

        when(gameRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> gameService.updateGame(dto));
    }

    @Test
    @DisplayName("findAllPaginatedGames - by name")
    void findAllPaginatedGames_byName() {
        when(gameRepository.findByNameContainingIgnoreCase(eq("fifa"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(game)));

        var page = gameService.findAllPaginatedGames(Pageable.unpaged(), "fifa");

        assertEquals(1, page.getTotalElements());
    }
}