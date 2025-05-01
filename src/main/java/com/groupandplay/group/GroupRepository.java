package com.groupandplay.group;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.groupandplay.game.Game;
import com.groupandplay.game.Platform;
import com.groupandplay.user.User;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    Optional<Group> findById(Integer id);

    List<Group> findByGame(Game game);

    List<Group> findByStatus(Status status);

    @Query("SELECT g FROM Group g JOIN g.users u WHERE u.username = :username")
    List<Group> findMyGroups(@Param("username") String username);

    @Query("SELECT COUNT(g) FROM Group g JOIN g.users u WHERE u.id = :userId AND g.status IN ('OPEN', 'CLOSED')")
    Integer findManyGroupsOpenOrClosed(@Param("userId") Integer userId);

    @Query("SELECT g FROM Group g " +
       "WHERE (:status IS NULL OR g.status = :status) " +
       "AND (:id IS NULL OR g.id = :id) " +
       "AND (:game IS NULL OR g.game = :game)")
    Page<Group> findFilteredGroups(@Param("status") Status status,
                                @Param("id") Integer id,
                                @Param("game") Game game,
                                Pageable pageable);

    @Query("SELECT g FROM Group g " +
       "WHERE g.status = :status " +
       "AND (:user NOT MEMBER OF g.users) " +
       "AND (:game IS NULL OR g.game = :game) " +
       "AND (:platform IS NULL OR g.platform = :platform) " +
       "AND (:communication IS NULL OR g.communication = :communication)")
    Page<Group> findFilteredOpenGroups(
        @Param("status") Status status,
        @Param("user") User user,
        @Param("game") Game game,
        @Param("communication") Communication communication,
        @Param("platform") Platform platform,
        Pageable pageable
    );


}
