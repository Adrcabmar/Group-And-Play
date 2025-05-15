package com.groupandplay.dto;

import com.groupandplay.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FriendDTO {
    private Integer id;
    private String username;
    private String profilePictureUrl;

    public FriendDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.profilePictureUrl = user.getProfilePictureUrl();
    }
}
