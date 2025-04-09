package com.groupandplay.group;

import java.time.LocalDateTime;
import java.util.Set;

import com.groupandplay.game.Game;
import com.groupandplay.model.BaseEntity;
import com.groupandplay.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "groups")
@Getter
@Setter
public class Group extends BaseEntity {

    @Enumerated(EnumType.STRING) 
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creation;

    @Enumerated(EnumType.STRING) 
    @Column(name = "comunication", nullable = false)
    private Communication communication;

    @Column(name = "description", nullable = true)
    @Size(min = 1, max = 256)
    private String description;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToMany(mappedBy = "groups", fetch = FetchType.EAGER)
    private Set<User> users;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;
}
