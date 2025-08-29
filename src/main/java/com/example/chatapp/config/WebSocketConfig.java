package com.example.chatapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // WebSocket handler registration will be implemented here later
        // for streaming chat functionality
        
        // Example:
        // registry.addHandler(new ChatWebSocketHandler(), "/ws/chat")
        //         .setAllowedOrigins("http://localhost:3000");
    }
}