package com.groupandplay.user;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.groupandplay.dto.ChangePasswordDTO;
import com.groupandplay.dto.EditUserDTO;
import com.groupandplay.dto.UserDTO;
import com.groupandplay.dto.UserMapper;
import com.groupandplay.game.GameRepository;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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


    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<UserDTO> dtoList = UserDTO.fromEntities(users);
        return ResponseEntity.ok(dtoList);
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

    /**
     * Registrar un nuevo usuario.
     */
    // @PostMapping("/register")
    // public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
    //     logger.info("Intentando registrar usuario: {}", user.getUsername());

    //     try {
    //         User newUser = userService.registerUser(user);
    //         return ResponseEntity.ok(newUser);
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.badRequest().body(e.getMessage());
    //     }
    // }

    /**
     * Actualizar un usuario existente.
     */
    @PutMapping("/{id}/edit")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @Valid @RequestBody EditUserDTO dto) {
        if (!hasRole("ROLE_ADMIN") && getCurrentUserLogged().getId() != id) {
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
        @RequestParam("file") MultipartFile file
    )  throws IllegalArgumentException {
        if (!hasRole("ROLE_ADMIN") && getCurrentUserLogged().getId() != id) {
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
        @Valid @RequestBody ChangePasswordDTO dto
    ) {
        User currentUser = getCurrentUserLogged(); 

        if (!hasRole("ROLE_ADMIN") && !currentUser.getId().equals(id)) {
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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
    }
}
