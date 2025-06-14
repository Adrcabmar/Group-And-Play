package com.groupandplay.group.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupandplay.chat.ChatRepository;
import com.groupandplay.config.JWTAuthFilter;
import com.groupandplay.dto.GroupDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;
import com.groupandplay.game.Platform;
import com.groupandplay.group.Communication;
import com.groupandplay.group.Group;
import com.groupandplay.group.GroupRepository;
import com.groupandplay.group.Status;
import com.groupandplay.invitation.InvitationRepository;
import com.groupandplay.message.MessageRepository;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Data;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({ "test", "mysql" })
class GroupControllerIntegrationTest {

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

    @MockBean
    private JWTAuthFilter jwtAuthFilter;

    private User user;
    private User user2;
    private Game game;

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

        user = new User();
        user.setUsername("bob456");
        user.setPassword("password");
        user.setFirstName("Bob");
        user.setLastName("Test");
        user.setEmail("bob@example.com");
        user.setRole("USER");
        user = userRepository.save(user);

        user2 = new User();
        user2.setUsername("charlie789");
        user2.setPassword("password");
        user2.setFirstName("charlie");
        user2.setLastName("Test");
        user2.setEmail("charlie@example.com");
        user2.setRole("USER");
        user2 = userRepository.save(user2);

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("adminpass");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@example.com");
        admin.setRole("ADMIN");
        userRepository.save(admin);

        game = new Game();
        game.setName("League of Legends");
        game.setMaxPlayers(5);
        game.setPlatforms(Set.of(Platform.PC));
        game = gameRepository.save(game);
    }

    // #region GET /api/groups/my-groups
    @Test
    @DisplayName("GET /api/groups/my-groups - success")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getMyGroups_success() throws Exception {
        mockMvc.perform(get("/api/groups/my-groups"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    // #endregion

    // #region POST /api/groups/create
    @Test
    @DisplayName("POST /api/groups/create - success")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void createGroup_success() throws Exception {
        GroupDTO dto = new GroupDTO();
        dto.setGameName(game.getName());
        dto.setPlatform("PC");
        dto.setUsergame("bob_game");
        dto.setStatus("OPEN");
        dto.setCommunication("NO_COMMUNICATION");

        mockMvc.perform(post("/api/groups/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameName").value(game.getName()));
    }

    @Test
    @DisplayName("POST /api/groups/create - invalid status")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void createGroup_invalidStatus() throws Exception {
        GroupDTO dto = new GroupDTO();
        dto.setGameName(game.getName());
        dto.setPlatform("PC");
        dto.setUsergame("bob_game");
        dto.setStatus("INVALID_STATUS");
        dto.setCommunication("TEXT");

        mockMvc.perform(post("/api/groups/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
    // #endregion

    // #region GET /api/groups/admin/all
    @Test
    @DisplayName("GET /api/groups/admin/all - success")
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getAllGroups_admin_success() throws Exception {
        mockMvc.perform(get("/api/groups/admin/all?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/groups/admin/all - forbidden for non-admin")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getAllGroups_nonAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/api/groups/admin/all?page=0&size=10"))
                .andExpect(status().isForbidden());
    }
    // #endregion

    // #region GET /api/groups/open
    @Test
    @DisplayName("GET /api/groups/open - success")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getOpenGroups_success() throws Exception {
        mockMvc.perform(get("/api/groups/open?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    // #endregion

    // #region POST /api/groups/join
    @Test
    @DisplayName("PUT /api/groups/join - success")
    @WithUserDetails(value = "charlie789", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void joinGroup_success() throws Exception {
        Group group = new Group();
        group.setGame(game);
        group.setPlatform(Platform.PLAYSTATION);
        group.setStatus(Status.OPEN);
        group.setCommunication(Communication.NO_COMMUNICATION);
        group.setUsergame("bob_game");
        group.setCreator(user);
        group.setCreation(LocalDateTime.now());
        group = groupRepository.save(group);

        mockMvc.perform(put("/api/groups/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(group.getId())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/groups/join - not found")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void joinGroup_notFound() throws Exception {
        mockMvc.perform(put("/api/groups/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("9999"))
                .andExpect(status().isBadRequest());
        ;
    }
    // #endregion

    // #region PUT /api/groups/edit
    @Test
    @DisplayName("PUT /api/groups/edit - success")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void editGroup_success() throws Exception {
        Group group = new Group();
        group.setGame(game);
        group.setPlatform(Platform.PLAYSTATION);
        group.setStatus(Status.OPEN);
        group.setCommunication(Communication.NO_COMMUNICATION);
        group.setUsergame("bob_game");
        group.setCreator(user);
        group.setCreation(LocalDateTime.now());
        group = groupRepository.save(group);

        GroupDTO updated = new GroupDTO();
        updated.setPlatform("PC");
        updated.setCommunication("NO_COMMUNICATION");
        updated.setUsergame("bob_updated");
        updated.setStatus("CLOSED");

        mockMvc.perform(put("/api/groups/edit/" + group.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usergame").value("bob_updated"));
    }

    @Test
    @DisplayName("PUT /api/groups/edit - not found")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void editGroup_notFound() throws Exception {
        GroupDTO updated = new GroupDTO();
        updated.setPlatform("PC");
        updated.setCommunication("TEXT");
        updated.setUsergame("bob_updated");
        updated.setStatus("CLOSED");

        mockMvc.perform(put("/api/groups/edit/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updated)))
                .andExpect(status().isBadRequest());
    }
    // #endregion

    // #region DELETE /api/groups/delete-my-group
    @Test
    @DisplayName("DELETE /api/groups/delete-my-group - success")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void deleteMyGroup_success() throws Exception {
        Group group = new Group();
        group.setGame(game);
        group.setPlatform(Platform.PLAYSTATION);
        group.setStatus(Status.OPEN);
        group.setCommunication(Communication.NO_COMMUNICATION);
        group.setUsergame("bob_game");
        group.setCreator(user);
        group.setCreation(LocalDateTime.now());
        group = groupRepository.save(group);

        mockMvc.perform(delete("/api/groups/delete-my-group/" + group.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/groups/delete-my-group - not found")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void deleteMyGroup_notFound() throws Exception {
        mockMvc.perform(delete("/api/groups/delete-my-group/9999"))
                .andExpect(status().isBadRequest());
    }
    // #endregion

    // #region DELETE /api/groups/leave-group
    @Test
    @DisplayName("PUT /api/groups/leave-group - success")
    @WithUserDetails(value = "charlie789", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void leaveGroup_success() throws Exception {
        Group group = new Group();
        group.setGame(game);
        group.setPlatform(Platform.PLAYSTATION);
        group.setStatus(Status.OPEN);
        group.setCommunication(Communication.NO_COMMUNICATION);
        group.setUsergame("bob_game");
        group.setCreator(user);
        group.setCreation(LocalDateTime.now());
        group.setUsers(new HashSet<>());

        User authenticatedUser = userRepository.findByUsername("charlie789").orElseThrow();
        authenticatedUser.setGroups(new HashSet<>());
        group.getUsers().add(authenticatedUser);
        authenticatedUser.getGroups().add(group);

        group = groupRepository.save(group);
        userRepository.save(authenticatedUser);

        mockMvc.perform(put("/api/groups/leave-group/" + group.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/groups/leave-group - not found")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void leaveGroup_notFound() throws Exception {
        mockMvc.perform(put("/api/groups/leave-group/9999"))
                .andExpect(status().isBadRequest());
    }
    // #endregion
}
