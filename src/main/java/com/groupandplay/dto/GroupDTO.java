package com.groupandplay.dto;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private String status;
    private LocalDateTime creation;
    private String communication;
    private String description;
    private String gameName;
    private Integer maxPlayers;
    private String creatorUsername;
    private String usergame;
    private List<String> users;
    private String platform;

    public GroupDTO(Group group) {
        this.id = group.getId();
        this.status = group.getStatus().toString();
        this.creation = group.getCreation();
        this.communication = group.getCommunication().toString();
        this.description = group.getDescription();
        this.gameName = group.getGame().getName();
        this.creatorUsername = group.getCreator().getUsername();
        this.maxPlayers = group.getGame().getMaxPlayers();
        this.platform = group.getPlatform().toString();
        this.usergame = group.getUsergame();
        this.users = group.getUsers().stream()
                          .map(user -> user.getUsername())
                          .collect(Collectors.toList());
    }

    public static List<GroupDTO> fromEntities(List<Group> groups) {
        return groups.stream()
                .map(GroupDTO::new)
                .toList();
    }
}
