
package com.groupandplay.group.unit;

import com.groupandplay.dto.GroupDTO;
import com.groupandplay.game.Game;
import com.groupandplay.game.GameRepository;
import com.groupandplay.game.Platform;
import com.groupandplay.group.Communication;
import com.groupandplay.group.Group;
import com.groupandplay.group.GroupRepository;
import com.groupandplay.group.GroupService;
import com.groupandplay.group.Status;
import com.groupandplay.invitation.InvitationRepository;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GroupServiceUnitTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private GroupService groupService;

    private User user;
    private Group group;
    private Game game;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1);
        user.setUsername("user");

        game = new Game();
        game.setId(1);
        game.setName("GameName");
        game.setPlatforms(Set.of(Platform.PC));

        group = new Group();
        group.setId(1);
        group.setUsers(new HashSet<>());
        group.setGame(game);
        group.setCreator(user);
        group.setCommunication(Communication.VOICE_CHAT);
        group.setPlatform(Platform.PC);
        group.setCreation(LocalDateTime.now());
    }

    // #region isMemberOfGroup
    @Test
    @DisplayName("isMemberOfGroup - true")
    void isMemberOfGroup_true() {
        group.getUsers().add(user);
        assertTrue(groupService.isMemberOfGroup(user, group));
    }

    @Test
    @DisplayName("isMemberOfGroup - false")
    void isMemberOfGroup_false() {
        assertFalse(groupService.isMemberOfGroup(user, group));
    }
    // #endregion

    // #region findById
    @Test
    @DisplayName("findById - grupo existente")
    void findById_success() {
        when(groupRepository.findById(1)).thenReturn(Optional.of(group));
        Group result = groupService.findById(1);
        assertEquals(group, result);
    }

    @Test
    @DisplayName("findById - grupo no encontrado")
    void findById_notFound() {
        when(groupRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> groupService.findById(1));
    }
    // #endregion

    // #region findMyGroups
    @Test
    @DisplayName("findMyGroups - usuario válido")
    void findMyGroups_valid() {
        when(groupRepository.findMyGroups("user")).thenReturn(Collections.singletonList(group));
        assertEquals(1, groupService.findMyGroups("user").size());
    }

    @Test
    @DisplayName("findMyGroups - username nulo")
    void findMyGroups_nullUsername() {
        assertThrows(IllegalArgumentException.class, () -> groupService.findMyGroups(null));
    }
    // #endregion

    // #region getAllGroups
    @Test
    @DisplayName("getAllGroups - éxito sin filtros")
    void getAllGroups_success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(groupRepository.findFilteredGroups(null, null, null, pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(group)));

        Page<Group> result = groupService.getAllGroups(pageable, null, null, null);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("getAllGroups - grupo no encontrado")
    void getAllGroups_invalidGroupId() {
        when(groupRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> groupService.getAllGroups(PageRequest.of(0, 5), 99, null, null));
    }

    @Test
    @DisplayName("getAllGroups - estado no válido")
    void getAllGroups_invalidStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> groupService.getAllGroups(PageRequest.of(0, 5), null, null, "INVALID"));
    }
    // #endregion

    // #region getFilteredOpenGroups

    @Test
    @DisplayName("getFilteredOpenGroups - éxito con filtros válidos")
    void getFilteredOpenGroups_success() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(gameRepository.findByName("GameName")).thenReturn(Optional.of(game));
        when(groupRepository.findFilteredOpenGroups(
                eq(com.groupandplay.group.Status.OPEN),
                eq(user),
                eq(game),
                eq(com.groupandplay.group.Communication.DISCORD),
                eq(com.groupandplay.game.Platform.PC),
                eq(pageable))).thenReturn(new PageImpl<>(Collections.singletonList(group)));

        Page<Group> result = groupService.getFilteredOpenGroups(
                pageable, "user", "GameName", "DISCORD", "PC");

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("getFilteredOpenGroups - usuario no encontrado")
    void getFilteredOpenGroups_userNotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> groupService.getFilteredOpenGroups(PageRequest.of(0, 5), "nouser", null, null, null));
    }
    // #endregion

    // #region createGroup

    @Test
    @DisplayName("createGroup - éxito")
    void createGroup_success() {
        GroupDTO dto = new GroupDTO();
        dto.setStatus("OPEN");
        dto.setPlatform("PC");
        dto.setGameName("GameName");
        dto.setCommunication("DISCORD");
        dto.setUsergame("nickname123");
        dto.setDescription("grupo test");

        game.setPlatforms(Set.of(com.groupandplay.game.Platform.PC));

        when(groupRepository.findManyGroupsOpenOrClosed(user.getId())).thenReturn(0);
        when(gameRepository.findByName("GameName")).thenReturn(Optional.of(game));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Group created = groupService.createGroup(dto, user);

        assertEquals(user, created.getCreator());
        assertEquals("grupo test", created.getDescription());
        assertTrue(created.getUsers().contains(user));
    }

    @Test
    @DisplayName("createGroup - usuario con 5 grupos")
    void createGroup_userLimitReached() {
        GroupDTO dto = new GroupDTO();
        when(groupRepository.findManyGroupsOpenOrClosed(user.getId())).thenReturn(5);
        assertThrows(IllegalArgumentException.class, () -> groupService.createGroup(dto, user));
    }
    // #endregion

    // #region joinGroup

    @Test
    @DisplayName("joinGroup - éxito sin invitación")
    void joinGroup_success() {
        group.setId(1);
        group.setStatus(Status.OPEN);
        group.setUsers(new HashSet<>());

        user.setId(1);
        user.setGroups(new HashSet<>());

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(groupRepository.findManyGroupsOpenOrClosed(user.getId())).thenReturn(0);
        when(groupRepository.save(any(Group.class))).thenReturn(group);
        when(userRepository.save(any(User.class))).thenReturn(user);

        Group joined = groupService.joinGroup(user, group, false);

        assertTrue(joined.getUsers().contains(user));
        assertTrue(user.getGroups().contains(group));
    }

    @Test
    @DisplayName("joinGroup - ya es miembro")
    void joinGroup_alreadyMember() {
        group.getUsers().add(user);
        assertThrows(IllegalArgumentException.class, () -> groupService.joinGroup(user, group, false));
    }

    @Test
    @DisplayName("joinGroup - grupo cerrado sin invitación")
    void joinGroup_closedGroupWithoutInvitation() {
        group.setStatus(Status.CLOSED);
        assertThrows(IllegalArgumentException.class, () -> groupService.joinGroup(user, group, false));
    }
    // #endregion

    // #region deleteMyGroup

    @Test
    @DisplayName("deleteMyGroup - éxito")
    void deleteMyGroup_success() {
        user.setGroups(new HashSet<>(Set.of(group)));
        group.setUsers(new HashSet<>(Set.of(user)));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        doNothing().when(invitationRepository).deleteAllByGroup(group);
        doNothing().when(groupRepository).delete(group);

        assertDoesNotThrow(() -> groupService.deleteMyGroup(user.getId(), group.getId()));
    }

    @Test
    @DisplayName("deleteMyGroup - usuario no es creador")
    void deleteMyGroup_notCreator() {
        User other = new User();
        other.setId(2);
        when(userRepository.findById(2)).thenReturn(Optional.of(other));
        when(groupRepository.findById(1)).thenReturn(Optional.of(group));

        assertThrows(IllegalArgumentException.class, () -> groupService.deleteMyGroup(2, 1));
    }
    // #endregion

    // #region leaveGroup

    @Test
    @DisplayName("leaveGroup - éxito")
    void leaveGroup_success() {
        User creator = new User();
        creator.setId(99);

        group.setCreator(creator); // el creador NO es quien abandona
        group.setUsers(new HashSet<>(Set.of(user, creator)));
        user.setGroups(new HashSet<>(Set.of(group)));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        assertDoesNotThrow(() -> groupService.leaveGroup(user.getId(), group.getId()));
        assertFalse(group.getUsers().contains(user));
        assertFalse(user.getGroups().contains(group));
    }

    @Test
    @DisplayName("leaveGroup - creador intenta salir")
    void leaveGroup_creatorCantLeave() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        group.getUsers().add(user);

        assertThrows(IllegalArgumentException.class, () -> groupService.leaveGroup(user.getId(), group.getId()));
    }
    // #endregion
}
