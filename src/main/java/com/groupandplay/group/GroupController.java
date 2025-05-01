package com.groupandplay.group;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.groupandplay.dto.GroupDTO;
import com.groupandplay.dto.UserDTO;
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

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals(role));
    }

    private User getCurrentUserLogged() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername(); 
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<Page<GroupDTO>> getAllGroups(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String game,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String status) {

        if(!hasRole("ADMIN")) {
            throw new IllegalArgumentException("No tienes permiso para ver todos los grupos");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> groups = groupService.getAllGroups(pageable, id, game, status);
        Page<GroupDTO> groupsDTO = groups.map(GroupDTO::new);
        return ResponseEntity.ok(groupsDTO);
    }


    @GetMapping("/open")
    public ResponseEntity<Page<GroupDTO>> getOpenGroups(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "3") int size,
        @RequestParam(required = false) String game,
        @RequestParam(required = false) String communication,
        @RequestParam(required = false) String platform

    ) {
        String username = getCurrentUserLogged().getUsername();
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> openGroups = groupService.getFilteredOpenGroups(pageable, username, game, communication, platform);
        Page<GroupDTO> openGroupsDTO = openGroups.map(GroupDTO::new);
        return ResponseEntity.ok(openGroupsDTO);
    }

    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupDTO>> getMyGroups() {
        String username = getCurrentUserLogged().getUsername();
        List<Group> openGroups = groupService.findMyGroups(username);

        List<GroupDTO> openGroupsDTO = GroupDTO.fromEntities(openGroups);

        return ResponseEntity.ok(openGroupsDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@Valid @RequestBody GroupDTO groupDTO) throws IllegalArgumentException {
        User creator = getCurrentUserLogged();

        Group newGroup = groupService.createGroup(groupDTO, creator);
        return ResponseEntity.ok(new GroupDTO(newGroup));
    }

    @PutMapping("/join")
    public ResponseEntity<?> joinGroup(@Valid @RequestBody Integer groupId) throws IllegalArgumentException {
        Group group = groupService.findById(groupId);
        User user = getCurrentUserLogged();
        Group groupUpdated = groupService.joinGroup(user, group);
        return ResponseEntity.ok(new GroupDTO(groupUpdated));
    }

    @PutMapping("/edit/{groupId}")
    public ResponseEntity<?> editGroup(@PathVariable Integer groupId, @Valid @RequestBody GroupDTO groupDTO) throws IllegalArgumentException {
        User user = getCurrentUserLogged();
        Group group = groupService.findById(groupId);

        if(!hasRole("ADMIN") && user.getId() != group.getCreator().getId()) {
            throw new IllegalArgumentException("No tienes permisos para editar este grupo");
        }

        Group groupUpdated = groupService.editGroup(group, groupDTO);
        return ResponseEntity.ok(new GroupDTO(groupUpdated));
    }

    @DeleteMapping("/delete-my-group/{groupId}")
    public ResponseEntity<?> deleteMyGroup(@PathVariable Integer groupId) throws IllegalArgumentException {
        User user = getCurrentUserLogged();
        groupService.deleteMyGroup(user.getId(), groupId);
        return ResponseEntity.ok("Grupo eliminado correctamente");
    }

    @PutMapping("/leave-group/{groupId}")
    public ResponseEntity<?> leaveGroup(@PathVariable Integer groupId) throws IllegalArgumentException {
        User user = getCurrentUserLogged(); 
        groupService.leaveGroup(user.getId(), groupId); 
        return ResponseEntity.ok("Has abandonado el grupo correctamente");
    }
}
