package com.groupandplay.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Integer chatId;
    private String senderUsername;
    private String content;
}
