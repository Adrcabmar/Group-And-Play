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
    private String description;
    private String profilePictureUrl;
    private String favGame;
    private List<GroupDTO> groups;
    private String role;
    private String discordName;

    
    public UserDTO(User user) {
        this.id = user.getId();
        this.lastname = user.getLastName();
        this.firstname = user.getFirstName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.description = user.getDescription();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.favGame = user.getFavGame() != null ? user.getFavGame().getName() : null;
        this.role = user.getRole();
        if(user.getGroups() != null){
            this.groups = user.getGroups().stream()
                    .map(GroupDTO::new)  
                    .collect(Collectors.toList());
        }
        this.discordName= user.getDiscordName();
    }

    public static List<UserDTO> fromEntities(List<User> users) {
        return users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }
}