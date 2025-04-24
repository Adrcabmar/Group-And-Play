package com.groupandplay.game;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groupandplay.dto.GroupDTO;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/games")
public class GameController {
    
    @Autowired
    private GameService gameService;

    @GetMapping("/all")
    public ResponseEntity<List<Game>> getGames() {
        List<Game> games = gameService.findAllGames();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/find/{gameName}")
    public ResponseEntity<Game> getGameByName(@PathVariable String gameName) {
        Game game = gameService.findByName(gameName);
        return ResponseEntity.ok(game);
    }
}
