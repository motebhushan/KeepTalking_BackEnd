package com.example.chatapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final WebClient.Builder webClientBuilder;

    @Value("${gemini.api.key:}")
    private String apiKey;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public String talkToGemini(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Gemini API key not found. Using mock response.");
            return "Hello! I'm a mock AI response since no API key is configured. Your message was: " + prompt;
        }

        try {
            WebClient webClient = webClientBuilder
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            String response = webClient
                    .post()
                    .uri(GEMINI_API_URL + "?key=" + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(this::extractTextFromResponse)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("Gemini API error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                        return Mono.just("Sorry, I'm having trouble connecting to the AI service right now.");
                    })
                    .onErrorResume(Exception.class, ex -> {
                        log.error("Unexpected error calling Gemini API", ex);
                        return Mono.just("Sorry, something went wrong. Please try again.");
                    })
                    .block();

            return response != null ? response : "Sorry, I couldn't process your request.";

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return "Sorry, I'm experiencing technical difficulties. Please try again later.";
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            var candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                var content = (Map<String, Object>) candidates.get(0).get("content");
                if (content != null) {
                    var parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
        }
        return "Sorry, I couldn't understand the response.";
    }
}
