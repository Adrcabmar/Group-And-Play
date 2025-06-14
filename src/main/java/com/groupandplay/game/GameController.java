package com.groupandplay.game;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.groupandplay.dto.GameDTO;
import com.groupandplay.dto.GroupDTO;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameService gameService;

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals(role));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Game>> getGames() {
        List<Game> games = gameService.findAllGames();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<Game>> getPaginatedGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(required = false) String gameName) {

        if (!hasRole("ADMIN")) {
            throw new IllegalArgumentException("No tienes permiso para acceder a este la lista paginada de juegos");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Game> games = gameService.findAllPaginatedGames(pageable, gameName);

        return ResponseEntity.ok(games);
    }

    @GetMapping("/find/{gameName}")
    public ResponseEntity<Game> getGameByName(@PathVariable String gameName) {

        Game game = gameService.findByName(gameName);
        return ResponseEntity.ok(game);
    }

    @GetMapping("/search/{gameId}")
    public ResponseEntity<Game> getGameById(@PathVariable Integer gameId) {

        Game game = gameService.findById(gameId);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/create")
    public ResponseEntity<Game> createGame(@RequestBody GameDTO gameDTO) {
        if (!hasRole("ADMIN")) {
            throw new IllegalArgumentException("No tienes permiso para crear un juego");
        }
        Game game = gameService.createGame(gameDTO);
        return ResponseEntity.ok(game);
    }

    @PutMapping("/edit")
    public ResponseEntity<Game> editGame(@RequestBody @Valid GameDTO gameDTO) {
        if (!hasRole("ADMIN")) {
            throw new IllegalArgumentException("No tienes permiso para editar un juego");
        }

        Game updatedGame = gameService.updateGame(gameDTO);
        return ResponseEntity.ok(updatedGame);
    }
}
