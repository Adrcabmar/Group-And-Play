package com.groupandplay.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.groupandplay.dto.ChangePasswordDTO;
import com.groupandplay.dto.EditUserDTO;
import com.groupandplay.dto.FriendDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;

import java.util.ArrayList;
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
        user.setDescription(dto.getDescription());
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
            if (oldPhoto != null && !oldPhoto.equals("/images/defecto.png")) {
                String filename = Paths.get(oldPhoto).getFileName().toString();
                Path oldPath = Paths.get("uploads/images", filename);
                Files.deleteIfExists(oldPath);
            }

            String fileName = "user_" + id + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
            Path uploadPath = Paths.get("uploads/images/");
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.setProfilePictureUrl("/images/" + fileName);

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

    // #region Amigos

    @Transactional
    public void removeFriend(User user, String friendUsername) {
        User userConAmigos = userRepository.findByIdWithFriends(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario logueado no encontrado"));

        User friend = userRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado al usuario " + friendUsername));

        if (!userRepository.areUsersFriends(userConAmigos, friend)) {
            throw new IllegalArgumentException(
                    "No sois amigos " + userConAmigos.getUsername() + " y " + friend.getUsername());
        }

        userConAmigos.removeFriend(friend);
        userRepository.save(userConAmigos);
        userRepository.save(friend);
    }

    @Transactional(readOnly = true)
    public Page<FriendDTO> searchFriends(User user, int page, int size, String username) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        Page<User> friendsPage = userRepository.findFriendsByUser(user, pageable);

        if (username != null && !username.isBlank()) {
            List<User> filtered = friendsPage.getContent().stream()
                    .filter(friend -> friend.getUsername() != null &&
                            friend.getUsername().toLowerCase().contains(username.toLowerCase()))
                    .toList();

            return new PageImpl<>(
                    filtered.stream().map(FriendDTO::new).toList(),
                    pageable,
                    filtered.size());
        }

        return friendsPage.map(FriendDTO::new);
    }

    // #endregion
}
