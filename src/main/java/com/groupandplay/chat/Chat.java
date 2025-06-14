package com.groupandplay.chat;

import com.groupandplay.game.Game;
import com.groupandplay.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Chat extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

}
