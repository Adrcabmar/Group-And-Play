package com.groupandplay.game.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupandplay.chat.ChatRepository;
import com.groupandplay.config.JWTAuthFilter;
import com.groupandplay.dto.GameDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;
import com.groupandplay.game.Platform;
import com.groupandplay.group.GroupRepository;
import com.groupandplay.invitation.InvitationRepository;
import com.groupandplay.message.MessageRepository;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({ "test", "mysql" })
public class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JWTAuthFilter jwtAuthFilter;

    private Game testGame;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());

        messageRepository.deleteAllInBatch();
        chatRepository.deleteAllInBatch();
        invitationRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        gameRepository.deleteAllInBatch();
        testGame = new Game();
        testGame.setName("TestGame");
        testGame.setMaxPlayers(10);
        testGame.setPlatforms(Set.of(Platform.PC));
        testGame = gameRepository.save(testGame);

        user = new User();
        user.setUsername("bob456");
        user.setPassword("password");
        user.setFirstName("Bob");
        user.setLastName("Test");
        user.setEmail("bob@example.com");
        user.setRole("USER");
        user = userRepository.save(user);
    }

    // region GET /api/games/all
    @Test
    @DisplayName("GET /api/games/all - success")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getAllGames_success() throws Exception {
        mockMvc.perform(get("/api/games/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("TestGame"));
    }
    // endregion

    // region GET /api/games/search/{gameId}
    @Test
    @DisplayName("GET /api/games/search/{gameId} - success")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getGameById_success() throws Exception {
        mockMvc.perform(get("/api/games/search/" + testGame.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TestGame"));
    }

    @Test
    @DisplayName("GET /api/games/search/{gameId} - not found")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getGameById_notFound() throws Exception {
        mockMvc.perform(get("/api/games/search/9999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
    // endregion

    // region GET /api/games/find/{gameName}
    @Test
    @DisplayName("GET /api/games/find/{gameName} - success")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getGameByName_success() throws Exception {
        mockMvc.perform(get("/api/games/find/TestGame"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TestGame"));
    }

    @Test
    @DisplayName("GET /api/games/find/{gameName} - not found")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getGameByName_notFound() throws Exception {
        mockMvc.perform(get("/api/games/find/NoExiste"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
    // endregion

    // region GET /api/games/paginated
    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("GET /api/games/paginated - success")
    void getPaginatedGames_success() throws Exception {
        mockMvc.perform(get("/api/games/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("TestGame"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    @DisplayName("GET /api/games/paginated - forbidden for USER")
    void getPaginatedGames_forbidden() throws Exception {
        mockMvc.perform(get("/api/games/paginated"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error").value("No tienes permiso para acceder a este la lista paginada de juegos"));
    }
    // endregion

    // region POST /api/games/create
    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("POST /api/games/create - success")
    void createGame_success() throws Exception {
        GameDTO dto = new GameDTO();
        dto.setName("NuevoJuego");
        dto.setMaxPlayers(16);
        dto.setPlatforms(Set.of("PC", "XBOX"));

        mockMvc.perform(post("/api/games/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NuevoJuego"))
                .andExpect(jsonPath("$.platforms").isArray());
    }

    @Test
    @WithMockUser(authorities = "USER")
    @DisplayName("POST /api/games/create - forbidden for USER")
    void createGame_forbidden() throws Exception {
        GameDTO dto = new GameDTO();
        dto.setName("JuegoProhibido");
        dto.setMaxPlayers(8);
        dto.setPlatforms(Set.of("PC"));

        mockMvc.perform(post("/api/games/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No tienes permiso para crear un juego"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("POST /api/games/create - invalid platform")
    void createGame_invalidPlatform() throws Exception {
        GameDTO dto = new GameDTO();
        dto.setName("JuegoErroneo");
        dto.setMaxPlayers(4);
        dto.setPlatforms(Set.of("INVALID_PLATFORM"));

        mockMvc.perform(post("/api/games/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Una o más plataformas no son válidas"));
    }
    // endregion

    // region PUT /api/games/edit
    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("PUT /api/games/edit - success")
    void editGame_success() throws Exception {
        GameDTO dto = new GameDTO();
        dto.setId(testGame.getId());
        dto.setName("GameEditado");
        dto.setMaxPlayers(20);
        dto.setPlatforms(Set.of("PC"));

        mockMvc.perform(put("/api/games/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("GameEditado"))
                .andExpect(jsonPath("$.maxPlayers").value(20));
    }

    @Test
    @WithMockUser(authorities = "USER")
    @DisplayName("PUT /api/games/edit - forbidden for USER")
    void editGame_forbidden() throws Exception {
        GameDTO dto = new GameDTO();
        dto.setId(testGame.getId());
        dto.setName("NoPermiso");
        dto.setMaxPlayers(10);
        dto.setPlatforms(Set.of("PC"));

        mockMvc.perform(put("/api/games/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No tienes permiso para editar un juego"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("PUT /api/games/edit - invalid ID")
    void editGame_invalidId() throws Exception {
        GameDTO dto = new GameDTO();
        dto.setId(9999);
        dto.setName("Fantasma");
        dto.setMaxPlayers(10);
        dto.setPlatforms(Set.of("PC"));

        mockMvc.perform(put("/api/games/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Juego no encontrado con ID: 9999"));
    }
    // endregion

}
