package com.example.chatapp.service;

import com.example.chatapp.dto.AnalyzeResponse;
import com.example.chatapp.dto.ChatResponse;
import com.example.chatapp.entity.Conversation;
import com.example.chatapp.entity.Message;
import com.example.chatapp.repository.ConversationRepository;
import com.example.chatapp.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final GeminiService geminiService;

    @Transactional
    public ChatResponse sendMessage(String sessionId, String userMessage) {
        // Get or create conversation
        String message="";
        Conversation conversation = conversationRepository
                .findBySessionId(sessionId)
                .orElseGet(() -> createNewConversation(sessionId));


        StringBuilder conversationContext = new StringBuilder();

        if (conversation.getMessages() != null) {
            for (Message msg : conversation.getMessages()) {
                conversationContext.append(msg.getSender().name())
                        .append(": ")
                        .append(msg.getText())
                        .append("\n");
            }
        }

        conversationContext.append("USER: ").append(userMessage).append("\n");
        String aiResponse = geminiService.talkToGemini(conversationContext.toString());

        // Save user message
        Message userMsg = Message.builder()
                .conversation(conversation)
                .sender(Message.Sender.USER)
                .text(userMessage)
                .createdAt(LocalDateTime.now())
                .build();
        userMsg = messageRepository.save(userMsg);



        // 🔥 Post-process to trim extra long responses
        aiResponse = cleanResponse(aiResponse);

        // Save AI message
        Message aiMsg = Message.builder()
                .conversation(conversation)
                .sender(Message.Sender.AI)
                .text(aiResponse)
                .createdAt(LocalDateTime.now())
                .build();
        aiMsg = messageRepository.save(aiMsg);

        return ChatResponse.builder()
                .reply(aiResponse)
                .sessionId(sessionId)
                .messageId(aiMsg.getId())
                .build();
    }

    public AnalyzeResponse analyzeConversation(String sessionId) {
        List<Message> messages = messageRepository.findByConversationSessionIdOrderByCreatedAtAsc(sessionId);

        if (messages.isEmpty()) {
            return AnalyzeResponse.builder()
                    .mistakes(Arrays.asList("No conversation found for analysis"))
                    .suggestions(Arrays.asList())
                    .vocabTips(Arrays.asList())
                    .build();
        }

        // Build conversation context for analysis
        String conversationText = messages.stream()
                .map(msg -> msg.getSender().name() + ": " + msg.getText())
                .collect(Collectors.joining("\n"));

        String analysisPrompt = buildAnalysisPrompt(conversationText);
        String analysis = geminiService.talkToGemini(analysisPrompt);

        return parseAnalysisResponse(analysis);
    }

    public List<Message> getChatHistory(String sessionId) {
        return messageRepository.findByConversationSessionIdOrderByCreatedAtAsc(sessionId);
    }

    private Conversation createNewConversation(String sessionId) {
        Conversation conversation = Conversation.builder()
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();
        return conversationRepository.save(conversation);
    }

    private String buildAnalysisPrompt(String conversationText) {
        return String.format(
                "Analyze this conversation for grammar mistakes, provide suggestions for improvement, and give vocabulary tips. " +
                        "Return your analysis in the following format:\n" +
                        "MISTAKES: [list grammar mistakes]\n" +
                        "SUGGESTIONS: [list improvement suggestions]\n" +
                        "VOCAB_TIPS: [list vocabulary tips]\n\n" +
                        "Conversation:\n%s",
                conversationText
        );
    }

    private AnalyzeResponse parseAnalysisResponse(String analysis) {
        try {
            List<String> mistakes = extractSection(analysis, "MISTAKES:");
            List<String> suggestions = extractSection(analysis, "SUGGESTIONS:");
            List<String> vocabTips = extractSection(analysis, "VOCAB_TIPS:");

            return AnalyzeResponse.builder()
                    .mistakes(mistakes.isEmpty() ? Arrays.asList("No significant grammar mistakes found") : mistakes)
                    .suggestions(suggestions.isEmpty() ? Arrays.asList("Keep practicing!") : suggestions)
                    .vocabTips(vocabTips.isEmpty() ? Arrays.asList("Continue expanding your vocabulary") : vocabTips)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing analysis response", e);
            return AnalyzeResponse.builder()
                    .mistakes(Arrays.asList("Analysis parsing error"))
                    .suggestions(Arrays.asList("Please try again"))
                    .vocabTips(Arrays.asList("Keep practicing"))
                    .build();
        }
    }

    private List<String> extractSection(String text, String sectionHeader) {
        String[] lines = text.split("\n");
        boolean inSection = false;
        List<String> items = new ArrayList<>();

        for (String line : lines) {
            if (line.trim().startsWith(sectionHeader)) {
                inSection = true;
                String content = line.substring(sectionHeader.length()).trim();
                if (!content.isEmpty()) {
                    items.addAll(Arrays.asList(content.split("[,;]")));
                }
                continue;
            }

            if (inSection && (line.trim().startsWith("MISTAKES:")
                    || line.trim().startsWith("SUGGESTIONS:")
                    || line.trim().startsWith("VOCAB_TIPS:"))) {
                break;
            }

            if (inSection && !line.trim().isEmpty()) {
                items.add(line.trim());
            }
        }

        return items.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // 🔥 Helper method to keep replies short and clean
    private String cleanResponse(String response) {
        if (response == null) return "Sorry, I could not process that.";
        // Limit to 500 characters
        if (response.length() > 500) {
            response = response.substring(0, 500) + "...";
        }
        // Limit to 3-4 lines max
        String[] lines = response.split("\n");
        if (lines.length > 4) {
            response = String.join("\n", Arrays.copyOfRange(lines, 0, 4));
        }
        return response.trim();
    }

    public String start(String topic) {
        String userMessage = "Let's practice English. The topic is \"" + topic
                + "\". Keep your replies short, clear, and natural. Do not use emojis. Speak like a human conversation partner.";
        String aiResponse = geminiService.talkToGemini(userMessage);
      return aiResponse;
    }

}
