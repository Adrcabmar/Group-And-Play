package com.groupandplay.group;

import java.time.LocalDateTime;
import java.util.HashSet;


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

    @Transactional(readOnly = true)
    public Group findById(Integer groupId) throws IllegalArgumentException {
        Group group = groupRepository.findById(groupId)                
            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        return group;
    }

    @Transactional(readOnly = true)
    public Page<Group> getOpenGroups(Pageable pageable, String username) throws IllegalArgumentException {
        User user = userRepository.findByUsername(username)
                     .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    
        return groupRepository.findOpenGroupsNotJoinedByUser(Status.OPEN, user, pageable);
    }

    @Transactional
    public Group createGroup(User creator, Game game, String communication, String description) {
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
        if (group.getStatus() != Status.OPEN) {
            throw new IllegalArgumentException("Grupo no disponible");
        }
    
        if (group.getUsers().contains(user)) {
            throw new IllegalArgumentException("Ya eres parte de este grupo");
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
    public void deleteMyGroup(User user, Group group) {
        if (group == null) {
            throw new IllegalArgumentException("El grupo no puede ser nulo");
        }
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
    
        if (!group.getCreator().getId().equals(user.getId())) {    
            throw new IllegalArgumentException("No puedes eliminar un grupo que no has creado");
        }
    
        groupRepository.delete(group);
        user.getGroups().remove(group);
        userRepository.save(user);
    }

    @Transactional
    public void leaveGroup(User user, Group group) {
        if (group == null) {
            throw new IllegalArgumentException("El grupo no puede ser nulo");
        }
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
    
        if (!group.getUsers().contains(user)) {
            throw new IllegalArgumentException("No eres miembro de este grupo");
        }
    
        if (group.getCreator().getId().equals(user.getId())) {
            throw new IllegalArgumentException("No puedes abandonar un grupo que has creado. Intenta eliminarlo en su lugar.");
        }
    
        group.getUsers().remove(user); 
        user.getGroups().remove(group);
    
        groupRepository.save(group);
        userRepository.save(user); 
    }

}
