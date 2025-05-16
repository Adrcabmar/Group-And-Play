package com.groupandplay.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponseDTO {
    private Integer id;
    private String content;
    private String senderUsername;
    private String senderProfilePictureUrl;
    private LocalDateTime date;
}
