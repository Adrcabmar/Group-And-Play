package com.groupandplay.webSocket;

import com.groupandplay.dto.ChatMessageDTO;
import com.groupandplay.dto.ChatMessageResponseDTO;
import com.groupandplay.chat.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat/{chatId}")
    public void receiveMessage(@DestinationVariable Integer chatId, ChatMessageDTO dto) {
        ChatMessageResponseDTO saved = chatService.saveMessage(chatId, dto);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, saved);
        
    }

    
}
