package com.example.chatapp.controller;

import com.example.chatapp.dto.ChatRequest;
import com.example.chatapp.dto.ChatResponse;
import com.example.chatapp.entity.Message;
import com.example.chatapp.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "https://keep-talking-navy.vercel.app")
public class ChatController {
    
    private final ChatService chatService;
    @PostMapping("/start")
    public String startChat(@RequestBody String topic){
       return chatService.start(topic);
    }
    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat message for session: {}", request.getSessionId());
        
        ChatResponse response = chatService.sendMessage(request.getSessionId(), request.getMessage());
        
        log.info("Sent response for session: {}", request.getSessionId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<Message>> getChatHistory(@RequestParam String sessionId) {
        log.info("Fetching chat history for session: {}", sessionId);
        
        List<Message> messages = chatService.getChatHistory(sessionId);
        
        log.info("Retrieved {} messages for session: {}", messages.size(), sessionId);
        return ResponseEntity.ok(messages);
    }
}