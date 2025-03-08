package com.groupandplay.dto;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.groupandplay.group.Comunication;
import com.groupandplay.group.Group;
import com.groupandplay.group.Status;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupDTO {
    private Integer id;
    private Status status;
    private LocalDateTime creation;
    private Comunication comunication;
    private String description;
    private Integer gameId;

    // Constructor para simplificar la creación del DTO
    public GroupDTO(Group group) {
        this.id = group.getId();
        this.status = group.getStatus();
        this.creation = group.getCreation();
        this.comunication = group.getComunication();
        this.description = group.getDescription();
        this.gameId = group.getGame().getId();
    }

    // Método estático para crear una lista de DTOs a partir de una lista de entidades
    public static List<GroupDTO> fromEntities(List<Group> events) {
        return events.stream()
                .map(GroupDTO::new)
                .toList();
    }
}
