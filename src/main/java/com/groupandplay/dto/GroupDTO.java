package com.groupandplay.dto;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.groupandplay.game.Game;
import com.groupandplay.group.Communication;
import com.groupandplay.group.Group;
import com.groupandplay.group.Status;
import com.groupandplay.user.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GroupDTO {
    private Integer id;
    private Status status;
    private LocalDateTime creation;
    private Communication communication;
    private String description;
    private String gameName;
    private Integer creatorId;

    public GroupDTO(Group group) {
        this.id = group.getId();
        this.status = group.getStatus();
        this.creation = group.getCreation();
        this.communication = group.getCommunication();
        this.description = group.getDescription();
        this.gameName = group.getGame().getName();
        this.creatorId = group.getCreator().getId();
    }

    public static List<GroupDTO> fromEntities(List<Group> events) {
        return events.stream()
                .map(GroupDTO::new)
                .toList();
    }
}
