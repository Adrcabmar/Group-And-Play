package com.groupandplay.user.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupandplay.config.JWTAuthFilter;
import com.groupandplay.dto.ChangePasswordDTO;
import com.groupandplay.dto.ReqRes;
import com.groupandplay.group.GroupRepository;
import com.groupandplay.invitation.InvitationRepository;
import com.groupandplay.message.MessageRepository;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({ "test", "mysql" })
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @MockBean
    private JWTAuthFilter jwtAuthFilter;

    private User adminUser;
    private User userBob;
    private User userDiana;

    @BeforeEach
    void setup() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());

        messageRepository.deleteAllInBatch();
        invitationRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        userDiana = new User();
        userDiana.setUsername("diana001");
        userDiana.setPassword("1234");
        userDiana.setRole("ADMIN");
        userDiana.setFirstName("Diana");
        userDiana.setLastName("López");
        userDiana.setEmail("diana@example.com");
        userDiana = userRepository.saveAndFlush(userDiana);

        userBob = new User();
        userBob.setUsername("bob456");
        userBob.setPassword("abcd");
        userBob.setRole("USER");
        userBob.setFirstName("Bob");
        userBob.setLastName("Smith");
        userBob.setEmail("bob@example.com");
        userBob = userRepository.saveAndFlush(userBob);

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123");
        adminUser.setRole("ADMIN");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("Admin");
        adminUser.setEmail("admin@example.com");
        adminUser = userRepository.saveAndFlush(adminUser);
    }

    // #region Registrarse
    @Test
    @DisplayName("POST /api/users/auth/register - Registro exitoso")
    void register_validUser_returns200() throws Exception {
        ReqRes request = new ReqRes();
        request.setFirstName("Juan");
        request.setLastName("Pérez");
        request.setUsername("juan123");
        request.setEmail("juan@example.com");
        request.setPassword("securePass");
        request.setRole("USER");

        mockMvc.perform(post("/api/users/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario guardado exitosamente"))
                .andExpect(jsonPath("$.user.username").value("juan123"));
    }

    @Test
    @DisplayName("POST /api/users/auth/register - Usuario duplicado")
    void register_duplicateUser_returns400InsideBody() throws Exception {
        ReqRes request = new ReqRes();
        request.setFirstName("Juan");
        request.setLastName("Pérez");
        request.setUsername("diana001");
        request.setEmail("diana@example.com");
        request.setPassword("securePass");
        request.setRole("USER");

        mockMvc.perform(post("/api/users/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error")
                        .value("El usuario con este nombre de usuario o correo electrónico."));
    }

    // //#endregion

    // #region Iniciar sesión

    @Test
    @DisplayName("POST /api/users/auth/login - Login exitoso")
    void login_validCredentials_returnsToken() throws Exception {
        User user = new User();
        user.setUsername("maria456");
        user.setEmail("maria@example.com");
        user.setFirstName("Maria");
        user.setLastName("Gómez");
        user.setPassword(new BCryptPasswordEncoder().encode("mypassword"));
        user.setRole("USER");
        userRepository.save(user);

        ReqRes loginRequest = new ReqRes();
        loginRequest.setUsername("maria456");
        loginRequest.setPassword("mypassword");

        mockMvc.perform(post("/api/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value("maria456"));
    }

    @Test
    @DisplayName("POST /api/users/auth/login - Login inválido")
    void login_wrongPassword_returns500StatusCodeInBody() throws Exception {
        User user = new User();
        user.setUsername("lucas789");
        user.setEmail("lucas@example.com");
        user.setFirstName("Lucas");
        user.setLastName("Gómez");
        user.setPassword(new BCryptPasswordEncoder().encode("correctpass"));
        user.setRole("USER");
        userRepository.save(user);

        ReqRes loginRequest = new ReqRes();
        loginRequest.setUsername("lucas789");
        loginRequest.setPassword("wrongpass");

        mockMvc.perform(post("/api/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk()) // HTTP 200 OK aunque sea un fallo lógico
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").exists());
    }

    // #endregion

    // #region Gets

    @Test
    @DisplayName("GET /api/users/{id} - OK")
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getUserById_existingUser_returnsOk() throws Exception {
        mockMvc.perform(get("/api/users/" + userDiana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("diana001"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - NOT FOUND")
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getUserById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/users/999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/username/{username} - OK")
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getUserByUsername_existingUser_returnsOk() throws Exception {
        mockMvc.perform(get("/api/users/username/" + userBob.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob456"));
    }

    @Test
    @DisplayName("GET /api/users/username/noexiste - NOT FOUND")
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getUserByUsername_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/users/username/noexiste"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/admin/all - como admin")
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getAllUsers_asAdmin_returnsUsers() throws Exception {
        mockMvc.perform(get("/api/users/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @Test
    @DisplayName("GET /api/users/admin/all - como user no permitido")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getAllUsers_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/users/admin/all"))
                .andExpect(status().isForbidden());
    }

    // #endregion

    // #region Contraseña

    @Test
    @DisplayName("PATCH /api/users/{id}/change-password - OK")
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void changePassword_validRequest_returnsOk() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setActualPassword("admin123");
        dto.setNewPassword("nuevaClaveSegura123");
        mockMvc.perform(patch("/api/users/" + adminUser.getId() + "/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Contraseña actualizada correctamente"));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/change-password")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void changePassword_wrongActualPassword_returns400() throws Exception {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setActualPassword("wrong");
        dto.setNewPassword("newPass");

        mockMvc.perform(patch("/api/users/" + userBob.getId() + "/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La contraseña actual es incorrecta"));
    }

    // #endregion

    // #region Foto

    @Test
    @DisplayName("POST /api/users/{id}/upload-photo - OK")
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void uploadPhoto_admin_returnsOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "fakeimagecontent".getBytes());

        mockMvc.perform(multipart("/api/users/" + adminUser.getId() + "/upload-photo")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Foto subida correctamente"));
    }

    @Test
    @DisplayName("POST /api/users/{id}/upload-photo - sin permisos")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void uploadPhoto_noPermission_returns403() throws Exception {
        mockMvc.perform(multipart("/api/users/" + adminUser.getId() + "/upload-photo")
                .file("file", "fake".getBytes()))
                .andExpect(status().isBadRequest());
    }

    // #endregion

    // #region Amigos

    @Test
    @DisplayName("GET /api/users/friends/all - OK con amigos")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getFriends_withFriends_returnsOk() throws Exception {
        userBob.addFriend(adminUser);
        adminUser.addFriend(userBob);
        userRepository.save(userBob);
        userRepository.save(adminUser);

        mockMvc.perform(get("/api/users/friends/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].username").value("admin"));
    }

    @Test
    @DisplayName("GET /api/users/friends/all - OK sin amigos")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getFriends_noFriends_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/users/friends/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("DELETE /api/users/friends/{username} - OK")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void deleteFriend_exists_returnsOk() throws Exception {
        userBob.addFriend(adminUser);
        adminUser.addFriend(userBob);
        userRepository.save(userBob);
        userRepository.save(adminUser);

        mockMvc.perform(delete("/api/users/friends/" + adminUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(content().string("Amigo eliminado correctamente."));
    }

    @Test
    @DisplayName("DELETE /api/users/friends/{username} - amigo no existe")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void deleteFriend_notExists_returns400() throws Exception {
        mockMvc.perform(delete("/api/users/friends/nope"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // #endregion
}