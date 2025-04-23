package com.groupandplay.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.groupandplay.user.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Integer id;
    private String lastname;
    private String firstname;
    private String username;
    private String email;
    private Integer telephone;
    private String profilePictureUrl;
    private String favGame;
    private List<GroupDTO> groups;
    
    // Constructor que toma la entidad User y la convierte a DTO
    public UserDTO(User user) {
        this.id = user.getId();
        this.lastname = user.getLastName();
        this.firstname = user.getFirstName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.telephone = user.getTelephone();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.favGame = user.getFavGame() != null ? user.getFavGame().getName() : null;
        if(user.getGroups() != null){
            this.groups = user.getGroups().stream()
                    .map(GroupDTO::new)  // Convertir la lista de eventos a DTOs
                    .collect(Collectors.toList());
        }
    }

    // Método estático para convertir una lista de usuarios en una lista de DTOs
    public static List<UserDTO> fromEntities(List<User> users) {
        return users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }
}