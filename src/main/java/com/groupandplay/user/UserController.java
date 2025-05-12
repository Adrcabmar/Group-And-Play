package com.groupandplay.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.groupandplay.dto.ChangePasswordDTO;
import com.groupandplay.dto.EditUserDTO;
import com.groupandplay.dto.FriendDTO;
import com.groupandplay.dto.PublicUserDTO;
import com.groupandplay.dto.UserDTO;
import com.groupandplay.dto.UserMapper;
import com.groupandplay.game.GameRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.checkerframework.checker.units.qual.h;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

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
    public ResponseEntity<Page<UserDTO>> getAllUsers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer id) {

        if (!hasRole("ADMIN")) {
            throw new IllegalArgumentException("No tienes permiso para ver todos los usuarios");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;

        if (username != null && !username.isEmpty()) {
            userPage = userService.findByUsernamePageable(username, pageable);
        } else if (id != null) {
            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                userPage = new PageImpl<>(List.of(userOptional.get()), pageable, 1);
            } else {
                return ResponseEntity.noContent().build();
            }
        } else {
            userPage = userService.getAllUsers(pageable);
        }

        if (userPage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Page<UserDTO> dtoPage = userPage.map(UserDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return ResponseEntity.ok(new UserDTO(user));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<User> user = Optional.ofNullable(userService.getUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicUserById(@PathVariable Integer id) {
        User target = userService.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    
        User userLogged = getCurrentUserLogged(); 

        if (userLogged.getId().equals(target.getId())) {
            Map<String, Object> response = new HashMap<>();
            response.put("self", true); 
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); 
        }
    
        boolean isFriend = userRepository.areUsersFriends(userLogged, target);
    
        PublicUserDTO dto = new PublicUserDTO(
            target.getUsername(),
            target.getFavGame() != null ? target.getFavGame().getName() : "Sin juego favorito",
            target.getProfilePictureUrl(),
            target.getDescription(),
            isFriend
        );
    
        return ResponseEntity.ok(dto);
    }

    /**
     * Actualizar un usuario existente.
     */
    @PutMapping("/{id}/edit")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @Valid @RequestBody EditUserDTO dto) {
        if (!hasRole("ADMIN") && getCurrentUserLogged().getId() != id) {
            throw new IllegalArgumentException("No tienes permiso para editar este usuario");
        }

        boolean usernameChanged = userService.checkIfUsernameChanged(id, dto.getUsername());

        User updatedUser = userService.updateUserFromDTO(id, dto);
        Map<String, Object> response = new HashMap<>();
        response.put("user", new UserDTO(updatedUser));
        response.put("usernameChanged", usernameChanged);

        return ResponseEntity.ok(response);
    }

    /**
     * Subir una foto.
     */
    @PostMapping("/{id}/upload-photo")
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) throws IllegalArgumentException {
        if (!hasRole("ADMIN") && getCurrentUserLogged().getId() != id) {
            throw new IllegalArgumentException("No tienes permiso para editar este usuario");
        }
        try {
            String mensaje = userService.uploadProfilePicture(id, file);
            return ResponseEntity.ok(mensaje);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/change-password")
    public ResponseEntity<String> changePassword(
            @PathVariable Integer id,
            @Valid @RequestBody ChangePasswordDTO dto) {
        User currentUser = getCurrentUserLogged();

        if (!hasRole("ADMIN") && !currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permiso para cambiar esta contraseña");
        }

        try {
            userService.changePassword(id, dto, currentUser);
            return ResponseEntity.ok("Contraseña actualizada correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Eliminar un usuario por ID.
     */
    // @DeleteMapping("/{id}")
    // public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
    //     try {
    //         userService.deleteUser(id);
    //         return ResponseEntity.ok("Usuario eliminado correctamente");
    //     } catch (Exception e) {
    //         return ResponseEntity.status(404).body("Usuario no encontrado");
    //     }
    // }

    // #region Amigos

    @GetMapping("/friends/all")
    public ResponseEntity<Page<FriendDTO>> getFriends(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String username) {
        User user = getCurrentUserLogged();
        Page<FriendDTO> friends = userService.searchFriends(user, page, size, username);
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/friends/{username}")
    public ResponseEntity<?> removeFriend(@PathVariable String username) {
        User user = getCurrentUserLogged();
        userService.removeFriend(user, username);
        return ResponseEntity.ok("Amigo eliminado correctamente.");
    }

    // #endregion
}
