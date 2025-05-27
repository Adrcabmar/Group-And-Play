package com.groupandplay.invitation.unit;

import com.groupandplay.dto.InvitationDTO;
import com.groupandplay.group.Group;
import com.groupandplay.group.GroupService;
import com.groupandplay.invitation.Invitation;
import com.groupandplay.invitation.InvitationRepository;
import com.groupandplay.invitation.InvitationService;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;
import com.groupandplay.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InvitationServiceTest {

    @InjectMocks
    private InvitationService invitationService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private InvitationRepository invitationRepository;

    private User sender;
    private User receiver;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        sender = new User();
        sender.setId(1);
        sender.setUsername("sender");
        receiver = new User();
        receiver.setId(2);
        receiver.setUsername("receiver");
    }

    // #region getMyInvitations

    @Test
    @DisplayName("getMyInvitations devuelve lista paginada")
    void getMyInvitations_success() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").descending());

        User sender = new User();
        sender.setId(1);
        sender.setUsername("sender123");

        Invitation invitation = new Invitation();
        invitation.setReceiver(receiver);
        invitation.setSender(sender);
        invitation.setDate(LocalDateTime.now());
        invitation.setGroupInvitation(false);

        when(invitationRepository.findAllByReceiver(eq(receiver), any()))
                .thenReturn(new PageImpl<>(List.of(invitation)));

        Page<InvitationDTO> result = invitationService.getMyInvitations(receiver, 0, 5);

        assertEquals(1, result.getTotalElements());
        verify(invitationRepository).findAllByReceiver(receiver, pageable);
    }

    // #endregion

    // #region createInvitation

    @Test
    @DisplayName("createInvitation crea invitación de amistad correctamente")
    void createInvitation_friendSuccess() {
        InvitationDTO dto = new InvitationDTO();
        dto.setReceiverUsername("receiver");
        dto.setGroupInvitation(false);

        when(userService.getUserByUsername("receiver")).thenReturn(Optional.of(receiver));
        when(userRepository.areUsersFriends(sender, receiver)).thenReturn(false);
        when(invitationRepository.existsByReceiverAndSenderAndGroupInvitation(receiver, sender, false))
                .thenReturn(false);
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Invitation result = invitationService.createInvitation(dto, sender);

        assertEquals(receiver, result.getReceiver());
        assertFalse(result.getGroupInvitation());
    }

    @Test
    @DisplayName("createInvitation lanza error si ya sois amigos")
    void createInvitation_friendAlreadyFriends() {
        InvitationDTO dto = new InvitationDTO();
        dto.setReceiverUsername("receiver");
        dto.setGroupInvitation(false);

        when(userService.getUserByUsername("receiver")).thenReturn(Optional.of(receiver));
        when(userRepository.areUsersFriends(sender, receiver)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> invitationService.createInvitation(dto, sender));
        assertEquals("Ya sois amigos.", ex.getMessage());
    }

    // #endregion

    // #region acceptInvitation

    @Test
    @DisplayName("acceptInvitation de amistad con éxito")
    void acceptFriendInvitation_success() {
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setReceiver(receiver);
        invitation.setSender(sender);
        invitation.setGroupInvitation(false);

        when(invitationRepository.findById(1)).thenReturn(Optional.of(invitation));
        when(userRepository.findByIdWithFriends(receiver.getId())).thenReturn(Optional.of(receiver));
        when(userRepository.areUsersFriends(receiver, sender)).thenReturn(false);

        invitationService.acceptInvitation(1, receiver.getId());

        verify(invitationRepository).delete(invitation);
    }

    @Test
    @DisplayName("acceptInvitation lanza error si ya son amigos")
    void acceptFriendInvitation_alreadyFriends() {
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setReceiver(receiver);
        invitation.setSender(sender);
        invitation.setGroupInvitation(false);

        when(invitationRepository.findById(1)).thenReturn(Optional.of(invitation));
        when(userRepository.findByIdWithFriends(receiver.getId())).thenReturn(Optional.of(receiver));
        when(userRepository.areUsersFriends(receiver, sender)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> invitationService.acceptInvitation(1, receiver.getId()));
        assertEquals("Ya sois amigos.", ex.getMessage());
    }

    // #endregion

    // #region rejectInvitation

    @Test
    @DisplayName("rejectInvitation con éxito")
    void rejectInvitation_success() {
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setReceiver(receiver);

        when(invitationRepository.findById(1)).thenReturn(Optional.of(invitation));

        invitationService.rejectInvitation(1, receiver);

        verify(invitationRepository).delete(invitation);
    }

    @Test
    @DisplayName("rejectInvitation de otra persona lanza error")
    void rejectInvitation_wrongUser() {
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setReceiver(new User() {
            {
                setId(99);
            }
        });

        when(invitationRepository.findById(1)).thenReturn(Optional.of(invitation));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> invitationService.rejectInvitation(1, receiver));
        assertEquals("No puedes rechazar una invitación que no es para ti.", ex.getMessage());
    }

    // #endregion
}
