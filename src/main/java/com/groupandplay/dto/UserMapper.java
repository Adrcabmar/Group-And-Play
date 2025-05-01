package com.groupandplay.dto;

import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;
import com.groupandplay.user.User;

import java.util.Optional;

public class UserMapper {

    public static User toEntity(UserDTO dto, GameRepository gameRepository) {
        User user = new User();
        user.setId(dto.getId());
        user.setFirstName(dto.getFirstname());
        user.setLastName(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setTelephone(dto.getTelephone());

        if (dto.getFavGame() != null && !dto.getFavGame().isBlank()) {
            Optional<Game> game = gameRepository.findByName(dto.getFavGame());
            game.ifPresent(user::setFavGame);
        }

        return user;
    }
}