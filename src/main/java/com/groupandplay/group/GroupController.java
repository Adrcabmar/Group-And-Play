package com.groupandplay.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;


import com.groupandplay.dto.GroupDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;

import jakarta.validation.Valid;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/groups")
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
            Group newGroup = groupService.createGroup(creator, game, groupDTO.getCommunication().toString(), groupDTO.getDescription());
            return ResponseEntity.ok( new GroupDTO(newGroup));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/open")
    public ResponseEntity<Page<GroupDTO>> getOpenGroups(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername(); // Extraer ID del usuario autenticado

        Pageable pageable = PageRequest.of(page, size);
        Page<Group> openGroups = groupService.getOpenGroups(pageable, username);

        Page<GroupDTO> openGroupsDTO = openGroups.map(GroupDTO::new);

        return ResponseEntity.ok(openGroupsDTO);
    }
}
