package com.groupandplay.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.groupandplay.chat.Chat;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    Optional<Message> findById(Integer id);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND (:before IS NULL OR m.date < :before) ORDER BY m.date DESC")
    List<Message> findMessagesBeforeDate(
            @Param("chat") Chat chat,
            @Param("before") LocalDateTime before,
            Pageable pageable);

}
