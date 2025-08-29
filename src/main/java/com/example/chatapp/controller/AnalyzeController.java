package com.example.chatapp.controller;

import com.example.chatapp.dto.AnalyzeRequest;
import com.example.chatapp.dto.AnalyzeResponse;
import com.example.chatapp.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:8081")
public class AnalyzeController {
    
    private final ChatService chatService;
    
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyzeConversation(@Valid @RequestBody AnalyzeRequest request) {
        log.info("Analyzing conversation for session: {}", request.getSessionId());
        
        AnalyzeResponse response = chatService.analyzeConversation(request.getSessionId());
        
        log.info("Analysis complete for session: {}", request.getSessionId());
        return ResponseEntity.ok(response);
    }
}