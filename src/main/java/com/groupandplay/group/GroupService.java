package com.groupandplay.group;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    
    public Group createGroup(User creator, Game game, Communication communication, String description) {
        Group group = new Group();
        group.setCreator(creator);
        group.setGame(game);
        group.setStatus(Status.OPEN);
        group.setCommunication(communication);
        group.setDescription(description);
        group.setCreation(LocalDateTime.now());
    
        if (group.getUsers() == null) {
            group.setUsers(new HashSet<>());
        }
        
        group.getUsers().add(creator);
    
        if (creator.getGroups() == null) {
            creator.setGroups(new HashSet<>());
        }
        creator.getGroups().add(group);
    
        group = groupRepository.save(group);
        
        userRepository.save(creator);
    
        return group;
    }
}
