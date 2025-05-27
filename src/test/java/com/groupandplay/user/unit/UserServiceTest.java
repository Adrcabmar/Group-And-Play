package com.groupandplay.user.unit;

import com.groupandplay.dto.ChangePasswordDTO;
import com.groupandplay.dto.EditUserDTO;
import com.groupandplay.dto.FriendDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;
import com.groupandplay.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashed_password");
        user.setRole("USER");
    }

    // 1. getAllUsers
    @Test
    void getAllUsers_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));

        Page<User> result = userService.getAllUsers(pageable);
        assertEquals(1, result.getContent().size());
    }

    // 2. getUserById
    @Test
    void getUserById_found() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        assertTrue(userService.getUserById(1).isPresent());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());
        assertTrue(userService.getUserById(2).isEmpty());
    }

    // 3. getUserByUsername
    @Test
    void getUserByUsername_found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        assertTrue(userService.getUserByUsername("testuser").isPresent());
    }

    @Test
    void getUserByUsername_notFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertTrue(userService.getUserByUsername("missing").isEmpty());
    }

    // 4. findByUsernamePageable
    @Test
    void findByUsernamePageable_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByUsernameContainingIgnoreCase("test", pageable))
                .thenReturn(new PageImpl<>(List.of(user)));

        Page<User> result = userService.findByUsernamePageable("test", pageable);
        assertEquals(1, result.getTotalElements());
    }

    // 5. isUsernameTaken
    @Test
    void isUsernameTaken_true() {
        when(userRepository.existsByUsername("taken")).thenReturn(true);
        assertTrue(userService.isUsernameTaken("taken"));
    }

    @Test
    void isUsernameTaken_false() {
        when(userRepository.existsByUsername("free")).thenReturn(false);
        assertFalse(userService.isUsernameTaken("free"));
    }

    // 6. isEmailTaken
    @Test
    void isEmailTaken_true() {
        when(userRepository.existsByEmail("mail")).thenReturn(true);
        assertTrue(userService.isEmailTaken("mail"));
    }

    @Test
    void isEmailTaken_false() {
        when(userRepository.existsByEmail("mail")).thenReturn(false);
        assertFalse(userService.isEmailTaken("mail"));
    }

    // 7. registerUser
    @Test
    void registerUser_success() {
        user.setPassword("plaintext");
        user.setId(null);
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.registerUser(user);
        assertNotEquals("plaintext", result.getPassword()); // encoded
    }

    @Test
    void registerUser_existingId_throws() {
        user.setId(123);
        assertThrows(RuntimeException.class, () -> userService.registerUser(user));
    }

    @Test
    void registerUser_usernameTaken_throws() {
        user.setId(null);
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> userService.registerUser(user));
    }

    @Test
    void registerUser_emailTaken_throws() {
        user.setId(null);
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> userService.registerUser(user));
    }

    // 8. updateUserFromDTO
    @Test
    void updateUserFromDTO_success() {
        EditUserDTO dto = new EditUserDTO();
        dto.setUsername("newuser");
        dto.setFirstname("John");
        dto.setLastname("Doe");
        dto.setEmail("john@doe.com");
        dto.setDescription("Nueva desc");
        dto.setProfilePictureUrl("/images/pic.png");
        dto.setFavGame(null);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User updated = userService.updateUserFromDTO(1, dto);
        assertEquals("newuser", updated.getUsername());
        assertEquals("john@doe.com", updated.getEmail());
    }

    @Test
    void updateUserFromDTO_userNotFound() {
        EditUserDTO dto = new EditUserDTO();
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.updateUserFromDTO(1, dto));
    }

    // 9. checkIfUsernameChanged
    @Test
    void checkIfUsernameChanged_true() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        assertTrue(userService.checkIfUsernameChanged(1, "anotheruser"));
    }

    @Test
    void checkIfUsernameChanged_false() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        assertFalse(userService.checkIfUsernameChanged(1, "testuser"));
    }

    @Test
    void checkIfUsernameChanged_notFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.checkIfUsernameChanged(1, "any"));
    }

    // 10. changePassword
    @Test
    void changePassword_success_owner() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setActualPassword("oldPassword");
        dto.setNewPassword("newPassword");
        User current = new User();
        current.setId(1);
        current.setRole("USER");

        user.setPassword(new BCryptPasswordEncoder().encode("oldPassword"));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.changePassword(1, dto, current);
        verify(userRepository).save(any());
    }

    @Test
    void changePassword_fail_wrongPassword() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setActualPassword("wrong");
        dto.setNewPassword("new");
        User current = new User();
        current.setId(1);
        current.setRole("USER");

        user.setPassword(new BCryptPasswordEncoder().encode("old"));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(1, dto, current));
    }

    // 11. deleteUser
    @Test
    void deleteUser_success() {
        userService.deleteUser(1);
        verify(userRepository).deleteById(1);
    }
}
