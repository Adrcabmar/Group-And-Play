package com.groupandplay.user.unit;

import com.groupandplay.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("john");
        user1.setRole("USER");

        user2 = new User();
        user2.setUsername("jane");
        user2.setRole("USER");
    }

    // ======= AMIGOS =======

    @Test
    void testAddFriend_positive() {
        user1.addFriend(user2);
        assertTrue(user1.getFriends().contains(user2));
        assertTrue(user2.getFriends().contains(user1));
    }

    @Test
    void testAddFriend_negative_nullFriend() {
        user1.addFriend(null);
        assertTrue(user1.getFriends().isEmpty());
    }

    @Test
    void testAddFriend_negative_selfFriend() {
        user1.addFriend(user1);
        assertTrue(user1.getFriends().isEmpty());
    }

    @Test
    void testRemoveFriend_positive() {
        user1.addFriend(user2);
        user1.removeFriend(user2);
        assertFalse(user1.getFriends().contains(user2));
        assertFalse(user2.getFriends().contains(user1));
    }

    @Test
    void testRemoveFriend_negative_nonExistentFriend() {
        user1.removeFriend(user2);
        assertTrue(user1.getFriends().isEmpty());
        assertTrue(user2.getFriends().isEmpty());
    }

    @Test
    void testRemoveFriend_negative_selfRemove() {
        user1.addFriend(user2);
        user1.removeFriend(user1);
        assertTrue(user1.getFriends().contains(user2));
    }

    @Test
    void testRemoveFriend_negative_nullRemove() {
        user1.addFriend(user2);
        user1.removeFriend(null);
        assertTrue(user1.getFriends().contains(user2));
    }

    @Test
    void testFriendsInitiallyEmpty_positive() {
        assertTrue(user1.getFriends().isEmpty());
    }

    @Test
    void testFriendsInitiallyEmpty_negative_afterAdd() {
        user1.addFriend(user2);
        assertFalse(user1.getFriends().isEmpty());
    }

    // ======= ROLES =======

    @Test
    void testGetAuthorities_positive() {
        assertEquals(
                Set.of(new SimpleGrantedAuthority("USER")),
                Set.copyOf(user1.getAuthorities()));
    }

    @Test
    void testGetAuthorities_negative_nullRole() {
        User user = new User();
        user.setRole(null);
        assertThrows(IllegalArgumentException.class, () -> user.getAuthorities());
    }

    // ======= FOTO DE PERFIL =======

    @Test
    void testDefaultProfilePictureUrl_positive() {
        User user = new User();
        assertEquals("/uploads/images/defecto.png", user.getProfilePictureUrl());
    }

    @Test
    void testCustomProfilePictureUrl_overridesDefault() {
        User user = new User();
        user.setProfilePictureUrl("/uploads/images/custom.png");
        assertEquals("/uploads/images/custom.png", user.getProfilePictureUrl());
    }

}
