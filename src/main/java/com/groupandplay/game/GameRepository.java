package com.groupandplay.game;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {

    Optional<Game> findById(Integer id);

    Optional<Game> findByName(String name);

    Page<Game> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Game> findAll(Pageable pageable);
}
