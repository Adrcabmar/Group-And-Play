package com.groupandplay.invitation;

import com.groupandplay.user.User;

import java.time.LocalDateTime;

import com.groupandplay.group.Group;
import com.groupandplay.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Invitation extends BaseEntity {

    @Column(name = "is_group_invitation", nullable = false)
    private Boolean groupInvitation;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
}

