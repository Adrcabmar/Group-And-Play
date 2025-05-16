package com.groupandplay.message;

import java.time.LocalDateTime;

import com.groupandplay.chat.Chat;
import com.groupandplay.model.BaseEntity;
import com.groupandplay.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Message extends BaseEntity{
    
    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "content", nullable = false)
    @NotBlank
    private String content;

    @Column(name = "date", nullable = false)
    private LocalDateTime date = LocalDateTime.now();

}
