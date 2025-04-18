package com.groupandplay.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.groupandplay.dto.EditUserDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
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

        return userRepository.save(user);
    }

    @Transactional
    public String uploadProfilePicture(Integer id, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
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
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
    
    
}
