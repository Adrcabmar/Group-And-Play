package com.groupandplay.group;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;

@Service
public class GroupService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

     @Autowired
    private GameRepository gameRepository;

    
     public Group createGroup(Integer creatorId, Integer gameId, Comunication communication, String description) {

        Optional<User> optionalUser = userRepository.findById(creatorId);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("El usuario con ID " + creatorId + " no existe.");
        }
        User creator = optionalUser.get();

        Optional<Game> optionalGame = gameRepository.findById(gameId);
        if (optionalGame.isEmpty()) {
            throw new RuntimeException("El juego con ID " + gameId + " no existe.");
        }

        Game game = optionalGame.get();
        Group group = new Group();
        
        group.setCreator(creator);
        group.setGame(game);
        group.setStatus(Status.OPEN);
        group.setComunication(communication);
        group.setDescription(description);

        return groupRepository.save(group);
    }
}
