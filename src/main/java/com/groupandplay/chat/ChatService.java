package com.groupandplay.chat;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.groupandplay.dto.ChatMessageDTO;
import com.groupandplay.dto.ChatMessageResponseDTO;
import com.groupandplay.message.Message;
import com.groupandplay.message.MessageRepository;
import com.groupandplay.user.User;
import com.groupandplay.user.UserRepository;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public ChatMessageResponseDTO saveMessage(Integer chatId, ChatMessageDTO dto) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        User sender = userRepository.findByUsername(dto.getSenderUsername()).orElseThrow();

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(dto.getContent());
        message.setDate(LocalDateTime.now());

        Message saved = messageRepository.save(message);

        ChatMessageResponseDTO response = new ChatMessageResponseDTO();
        response.setId(saved.getId());
        response.setContent(saved.getContent());
        response.setSenderUsername(sender.getUsername());
        response.setSenderProfilePictureUrl(sender.getProfilePictureUrl());
        response.setDate(saved.getDate());

        return response;
    }

    public List<ChatMessageResponseDTO> getMessagesBefore(Integer chatId, LocalDateTime before, int limit) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat no encontrado"));

        Pageable pageable = PageRequest.of(0, limit);
        List<Message> messages = messageRepository.findMessagesBeforeDate(chat, before, pageable);

        // Opcional: invertir para mostrar en orden ascendente (antiguo → nuevo)
        Collections.reverse(messages);

        return messages.stream().map(message -> {
            ChatMessageResponseDTO dto = new ChatMessageResponseDTO();
            dto.setId(message.getId());
            dto.setContent(message.getContent());
            dto.setSenderUsername(message.getSender().getUsername());
            dto.setSenderProfilePictureUrl(message.getSender().getProfilePictureUrl());
            dto.setDate(message.getDate());
            return dto;
        }).toList();
    }
}
