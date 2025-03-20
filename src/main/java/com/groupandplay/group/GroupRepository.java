package com.groupandplay.group;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.groupandplay.game.Game;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    Optional<Group> findById(Integer id);

    List<Group> findByGame(Game game);

    List<Group> findByStatus(Status status);

    Page<Group> findByStatusAndCreatorIdNot(Status status, Integer creatorId, Pageable pageable);


}
