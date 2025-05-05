package com.groupandplay.group;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.checkerframework.checker.units.qual.g;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.groupandplay.dto.GroupDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;
import com.groupandplay.game.Platform;
import com.groupandplay.invitation.InvitationRepository;
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

    @Autowired
    private InvitationRepository invitationRepository;

    public boolean isMemberOfGroup(User user, Group group) {
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
    public Page<Group> getAllGroups(Pageable pageable, Integer id, String gameName, String statusStr) throws IllegalArgumentException {
        if (id != null) {
            groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
        }
    
        Game game = null;
        if (gameName != null && !gameName.isEmpty()) {
            game = gameRepository.findByName(gameName)
                                 .orElse(null);
        }
    
        Status status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = Status.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Estado no válido: " + statusStr);
            }
        }
    
        return groupRepository.findFilteredGroups(status, id, game, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Group> getFilteredOpenGroups(Pageable pageable, String username, String gameName, String communicationStr,String platformStr) throws IllegalArgumentException {
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

        Platform platform = null;
        if (platformStr != null && !platformStr.isEmpty()) {
            platform = Platform.valueOf(platformStr);
        }

        return groupRepository.findFilteredOpenGroups(Status.OPEN, user, game, communication, platform, pageable);
    }

    @Transactional
    public Group createGroup(GroupDTO groupDTO, User creator) throws IllegalArgumentException {

        if (groupRepository.findManyGroupsOpenOrClosed(creator.getId()) >= 5) {
            throw new IllegalArgumentException("Ya formas parte de 5 grupos, abandona alguno para crear otro");
        }

        Platform selectedPlatform = Platform.valueOf(groupDTO.getPlatform());

        Game game = gameRepository.findByName(groupDTO.getGameName())
            .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
        if (!game.getPlatforms().contains(selectedPlatform)) {
            throw new IllegalArgumentException("La plataforma seleccionada no está disponible para este juego");
        }

        Group group = new Group();
        group.setCreator(creator);
        group.setGame(game);
        group.setStatus(Status.OPEN);
        group.setPlatform(selectedPlatform);
        group.setCommunication(Communication.valueOf(groupDTO.getCommunication()));
        group.setUsergame(groupDTO.getUsergame());
        group.setDescription(groupDTO.getDescription());
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
        if (groupRepository.findManyGroupsOpenOrClosed(user.getId()) >= 6) {
            throw new IllegalArgumentException("Ya formas parte de 6 grupos, abandona alguno para unirte a este");
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
    public Group editGroup(Group group, GroupDTO groupDTO) throws IllegalArgumentException {
        if (group == null || groupDTO == null) {
            throw new IllegalArgumentException("Ha ocurrido un problema al editar el grupo");
        }

        if (group.getGame() != null && groupDTO.getPlatform() != null) {
            boolean platformValida = group.getGame().getPlatforms().stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(groupDTO.getPlatform()));
            if (!platformValida) {
                throw new IllegalArgumentException("La plataforma seleccionada no está entre las disponibles del juego.");
            }

            group.setPlatform(Platform.valueOf(groupDTO.getPlatform().toUpperCase()));
        }

        if (groupDTO.getDescription() != null) {
            group.setDescription(groupDTO.getDescription());
        }

        if (groupDTO.getUsergame() != null) {
            group.setUsergame(groupDTO.getUsergame());
        }

        if (groupDTO.getCommunication() != null) {
            String commUpper = groupDTO.getCommunication().toUpperCase();
            try {
                group.setCommunication(Communication.valueOf(commUpper));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Tipo de comunicación inválido: " + commUpper);
            }
        }
        

        if (groupDTO.getStatus() != null) {
            String status = groupDTO.getStatus().toUpperCase();
        
            if (!status.equals("OPEN") && !status.equals("CLOSED")) {
                throw new IllegalArgumentException("El estado del grupo debe ser OPEN o CLOSED.");
            }
        
            group.setStatus(Status.valueOf(status));
        }

        return groupRepository.save(group);
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

        invitationRepository.deleteAllByGroup(group);

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
