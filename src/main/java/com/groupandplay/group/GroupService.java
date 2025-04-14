package com.groupandplay.group;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

    private boolean isMemberOfGroup(User user, Group group) {
        return group.getUsers().stream()
        .anyMatch(u -> u.getId().equals(user.getId()));    
    }

    @Transactional(readOnly = true)
    public Group findById(Integer groupId) throws IllegalArgumentException {
        Group group = groupRepository.findById(groupId)                
            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        return group;
    }

    @Transactional(readOnly = true )
    public List<Group> findMyGroups(String username)  throws IllegalArgumentException {
        if (username == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        return groupRepository.findMyGroups(username);
    }

    @Transactional(readOnly = true)
    public Page<Group> getFilteredOpenGroups(Pageable pageable, String username, String gameName, String communicationStr) {
        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Game game = null;
        if (gameName != null && !gameName.isEmpty()) {
            game = gameRepository.findByName(gameName)
                                .orElse(null); // puede ser null si no se encuentra
        }

        Communication communication = null;
        if (communicationStr != null && !communicationStr.isEmpty()) {
            communication = Communication.valueOf(communicationStr);
        }

        return groupRepository.findFilteredOpenGroups(Status.OPEN, user, game, communication, pageable);
    }

    @Transactional
    public Group createGroup(User creator, Game game, String communication, String description) throws IllegalArgumentException {

        if (groupRepository.findManyGroupsOpenOrClosed(creator.getId()) >= 5) {
            throw new IllegalArgumentException("Ya formas parte de 5 grupos, abandona alguno para crear otro");
        }

        Group group = new Group();
        group.setCreator(creator);
        group.setGame(game);
        group.setStatus(Status.OPEN);
        group.setCommunication(Communication.valueOf(communication));
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

    public Group joinGroup(User user, Group group) throws IllegalArgumentException {

        if (isMemberOfGroup(user, group)) {
            throw new IllegalArgumentException("Ya eres parte de este grupo");
        }
        if (groupRepository.findManyGroupsOpenOrClosed(user.getId()) >= 5) {
            throw new IllegalArgumentException("Ya formas parte de 5 grupos, abandona alguno para unirte a este");
        }
        if (group.getStatus() != Status.OPEN) {
            throw new IllegalArgumentException("Grupo no disponible");
        }

        group.getUsers().add(user);
    
        if (user.getGroups() == null) {
            user.setGroups(new HashSet<>());
        }
        user.getGroups().add(group);
    
        group = groupRepository.save(group);
        
        userRepository.save(user);
    
        return group;
    }

    @Transactional
    public void deleteMyGroup(Integer userId, Integer groupId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));

        if (!group.getCreator().getId().equals(user.getId())) {
            throw new IllegalArgumentException("No puedes eliminar un grupo que no has creado");
        }

        for (User u : group.getUsers()) {
            u.getGroups().remove(group);
        }
        group.getUsers().clear(); 

        groupRepository.delete(group); 
    }
    @Transactional
    public void leaveGroup(Integer userId, Integer groupId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        
        if (!isMemberOfGroup(user, group)) {
            throw new IllegalArgumentException("No eres miembro de este grupo");
        }
    
        if (group.getCreator().getId().equals(user.getId())) {
            throw new IllegalArgumentException("No puedes abandonar un grupo que has creado. Intenta eliminarlo en su lugar.");
        }

        
        user.getGroups().remove(group);
        group.getUsers().remove(user);

        userRepository.save(user);
    }

}
