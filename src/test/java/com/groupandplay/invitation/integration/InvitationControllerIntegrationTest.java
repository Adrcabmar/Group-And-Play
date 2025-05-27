package com.groupandplay.invitation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupandplay.config.JWTAuthFilter;
import com.groupandplay.dto.InvitationDTO;
import com.groupandplay.group.GroupRepository;
import com.groupandplay.invitation.Invitation;
import com.groupandplay.invitation.InvitationRepository;
import com.groupandplay.message.MessageRepository;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({ "test", "mysql" })
class InvitationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MessageRepository messageRepository;

    @MockBean
    private JWTAuthFilter jwtAuthFilter;

    private User sender;
    private User receiver;

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

        sender = new User();
        sender.setUsername("bob456");
        sender.setPassword("password");
        sender.setFirstName("Bob");
        sender.setLastName("Sender");
        sender.setEmail("bob@example.com");
        sender.setRole("USER");
        sender = userRepository.saveAndFlush(sender);

        receiver = new User();
        receiver.setUsername("diana001");
        receiver.setPassword("password");
        receiver.setFirstName("Diana");
        receiver.setLastName("Receiver");
        receiver.setEmail("diana@example.com");
        receiver.setRole("USER");
        receiver = userRepository.saveAndFlush(receiver);
    }

    // #region Crear invitaciones

    @Test
    @DisplayName("POST /api/invitations/create - OK amistad")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void createFriendInvitation_returnsOk() throws Exception {
        InvitationDTO dto = new InvitationDTO();
        dto.setReceiverUsername("diana001");
        dto.setGroupInvitation(false);

        mockMvc.perform(post("/api/invitations/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverUsername").value("diana001"));
    }

    @Test
    @DisplayName("POST /api/invitations/create - invitación a sí mismo")
    @WithUserDetails(value = "bob456", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void createInvitationToSelf_returns400() throws Exception {
        InvitationDTO dto = new InvitationDTO();
        dto.setReceiverUsername("bob456");
        dto.setGroupInvitation(false);

        mockMvc.perform(post("/api/invitations/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("No puedes enviarte una invitación a ti mismo."));
    }

    // #endregion

    // #region Obtener invitaciones

    @Test
    @DisplayName("GET /api/invitations/all-invitations - OK")
    @WithUserDetails(value = "diana001", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void getAllInvitations_returnsOk() throws Exception {
        Invitation invitation = new Invitation();
        invitation.setSender(sender);
        invitation.setReceiver(receiver);
        invitation.setGroupInvitation(false);
        invitation.setDate(LocalDateTime.now());
        invitationRepository.save(invitation);

        mockMvc.perform(get("/api/invitations/all-invitations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    // #endregion

    // #region Aceptar/rechazar

    @Test
    @DisplayName("POST /api/invitations/accept/{id} - OK")
    @WithUserDetails(value = "diana001", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void acceptInvitation_returnsOk() throws Exception {
        Invitation invitation = new Invitation();
        invitation.setSender(sender);
        invitation.setReceiver(receiver);
        invitation.setGroupInvitation(false);
        invitation.setDate(LocalDateTime.now());
        Invitation saved = invitationRepository.save(invitation);

        mockMvc.perform(post("/api/invitations/accept/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Invitación aceptada correctamente."));
    }

    @Test
    @DisplayName("POST /api/invitations/reject/{id} - OK")
    @WithUserDetails(value = "diana001", userDetailsServiceBeanName = "userDetailsServiceImpl")
    void rejectInvitation_returnsOk() throws Exception {
        Invitation invitation = new Invitation();
        invitation.setSender(sender);
        invitation.setReceiver(receiver);
        invitation.setGroupInvitation(false);
        invitation.setDate(LocalDateTime.now());
        Invitation saved = invitationRepository.save(invitation);

        mockMvc.perform(post("/api/invitations/reject/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Invitación rechazada correctamente."));
    }

    // #endregion
}
