package com.groupandplay.dto;

import org.checkerframework.checker.units.qual.s;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditUserDTO {
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private Integer telephone;
    private String favGame;
    private String profilePictureUrl;
}
