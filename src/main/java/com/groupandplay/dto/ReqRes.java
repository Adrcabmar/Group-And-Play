package com.groupandplay.dto;

import java.util.List;

import com.groupandplay.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqRes {
    
    private int statusCode;
    private String error;
    private String token;
    private String refreshToken;
    private String expirationTime;
    private String message;

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String role;

    private UserDTO user;
    private List<User> usersList;    
}
