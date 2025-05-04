package com.groupandplay.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.groupandplay.dto.ChangePasswordDTO;
import com.groupandplay.dto.EditUserDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;

import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.*;

import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Page<User> findByUsernamePageable(String username, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User registerUser(User user) {
        if (user.getId() != null) {
            throw new RuntimeException("No se puede registrar un usuario con ID preexistente");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("El username ya está en uso");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserFromDTO(Integer id, EditUserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstname());
        user.setLastName(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setTelephone(dto.getTelephone());
        user.setProfilePictureUrl(dto.getProfilePictureUrl());

        if (dto.getFavGame() != null && !dto.getFavGame().isBlank()) {
            gameRepository.findByName(dto.getFavGame()).ifPresent(user::setFavGame);
        } else {
            user.setFavGame(null);
        }

        userRepository.save(user);

        return user;
    }

    public boolean checkIfUsernameChanged(Integer id, String newUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return !user.getUsername().equals(newUsername);
    }

    @Transactional
    public String uploadProfilePicture(Integer id, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png")
                        || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Formato no permitido. Solo se permiten imágenes JPG, PNG o WEBP.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {

            String oldPhoto = user.getProfilePictureUrl();
            if (oldPhoto != null && !oldPhoto.equals("/resources/images/defecto.png")) {
                Path oldPath = Paths.get("src/main/resources/static" + oldPhoto);
                try {
                    Files.deleteIfExists(oldPath);
                } catch (IOException ex) {
                    System.err.println("⚠ No se pudo eliminar la imagen anterior: " + oldPath);
                }
            }
            String fileName = "user_" + id + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
            Path uploadPath = Paths.get("src/main/resources/static/resources/images/");
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.setProfilePictureUrl("/resources/images/" + fileName);
            userRepository.save(user);

            return "Foto subida correctamente";
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen", e);
        }
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordDTO dto, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean isAdmin = currentUser.getRole().equals("ADMIN");
        boolean isOwner = currentUser.getId().equals(userId);

        if (isOwner && !isAdmin) {
            if (dto.getActualPassword() == null || dto.getActualPassword().isBlank()) {
                throw new IllegalArgumentException("Debes introducir la contraseña actual");
            }

            if (!passwordEncoder.matches(dto.getActualPassword(), user.getPassword())) {
                throw new IllegalArgumentException("La contraseña actual es incorrecta");
            }
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void removeFriend(User user, String friendUsername) {
        User friend = userRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado al usuario " + friendUsername));

        if (!user.getFriends().contains(friend)) {
            throw new IllegalArgumentException(friendUsername + " no está en tu lista de amigos.");
        }

        user.removeFriend(friend);
        userRepository.save(user);
        userRepository.save(friend);
    }

}
