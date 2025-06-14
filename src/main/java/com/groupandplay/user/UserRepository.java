package com.groupandplay.user;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findById(Integer id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    @Query("SELECT f FROM User u JOIN u.friends f WHERE u = :user")
    Page<User> findFriendsByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.friends f WHERE u = :creator AND f = :receiver")
    boolean areUsersFriends(@Param("creator") User creator, @Param("receiver") User receiver);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.friends WHERE u.id = :id")
    Optional<User> findByIdWithFriends(@Param("id") Integer id);

    @Query("SELECT COUNT(g) > 0 FROM Group g WHERE g.creator.id = :userId AND g.communication = 'DISCORD'")
    boolean isCreatorOfDiscordGroup(@Param("userId") Integer userId);
}
