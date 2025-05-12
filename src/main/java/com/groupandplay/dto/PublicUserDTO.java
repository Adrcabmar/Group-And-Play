package com.groupandplay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PublicUserDTO {
    private String username;
    private String favGame;
    private String profilePictureUrl;
    private String description;
    private boolean isFriend;
}
