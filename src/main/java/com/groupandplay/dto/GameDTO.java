package com.groupandplay.dto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.groupandplay.game.Game;
import com.groupandplay.game.Platform;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GameDTO {
    private Integer id;
    private String name;
    private Integer maxPlayers;
    private Set<String> platforms;

    public GameDTO(Game game) {
        this.id = game.getId();
        this.name = game.getName();
        this.maxPlayers = game.getMaxPlayers();
        this.platforms = game.getPlatforms()
                             .stream()
                             .map(Platform::name)
                             .collect(Collectors.toSet());
    }

    public static List<GameDTO> fromEntities(List<Game> games) {
        return games.stream()
                .map(GameDTO::new)
                .toList();
    }
}