package com.groupandplay.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groupandplay.dto.GroupDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;

import jakarta.validation.Valid;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/group")
public class GroupController {
    
    @Autowired
    private GroupService groupService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@Valid @RequestBody GroupDTO groupDTO) {
        try {
            User creator = userRepository.findById(groupDTO.getCreatorId())
            .orElseThrow(() -> new IllegalArgumentException("Usuario creador no encontrado"));

            Game game = gameRepository.findByName(groupDTO.getGameName())
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));

            // Crear el grupo con los datos correctos
            Group newGroup = groupService.createGroup(creator, game, groupDTO.getCommunication(), groupDTO.getDescription());
            return ResponseEntity.ok( new GroupDTO(newGroup));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
