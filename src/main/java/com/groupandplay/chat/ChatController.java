package com.groupandplay.chat;

import com.groupandplay.dto.ChatMessageResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<ChatMessageResponseDTO>> getMessages(
            @PathVariable Integer chatId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before,
            @RequestParam(defaultValue = "20") int limit) {
        List<ChatMessageResponseDTO> messages = chatService.getMessagesBefore(chatId, before, limit);
        return ResponseEntity.ok(messages);
    }
}
