# Spring Boot Chat Application with Gemini Integration

A production-ready Spring Boot application that provides chat functionality with Google Gemini AI integration, conversation analysis, and message history.

## Features

- **Chat Interface**: Send messages and receive AI responses via REST API
- **Conversation Analysis**: Analyze conversations for grammar mistakes, suggestions, and vocabulary tips
- **Message History**: Retrieve full conversation history by session ID
- **UUID-based Sessions**: Frontend generates session IDs for conversation grouping
- **Production Ready**: Clean architecture with proper error handling and logging
- **Database Support**: H2 for development, PostgreSQL for production

## Tech Stack

- Spring Boot 3.2.1
- Spring Web & WebFlux
- Spring Data JPA
- H2 Database (dev) / PostgreSQL (prod)
- Lombok
- Bean Validation
- WebSocket support (stub for future streaming)

## Project Structure

```
src/main/java/com/example/chatapp/
├── ChatApplication.java
├── config/
│   ├── WebConfig.java (CORS configuration)
│   └── WebSocketConfig.java (WebSocket stub)
├── controller/
│   ├── ChatController.java
│   └── AnalyzeController.java
├── dto/
│   ├── ChatRequest.java
│   ├── ChatResponse.java
│   ├── AnalyzeRequest.java
│   └── AnalyzeResponse.java
├── entity/
│   ├── Conversation.java
│   └── Message.java
├── repository/
│   ├── ConversationRepository.java
│   └── MessageRepository.java
└── service/
    ├── GeminiService.java
    └── ChatService.java
```

## API Endpoints

### Send Message
```
POST /api/chat/send
Content-Type: application/json

{
  "sessionId": "uuid-here",
  "message": "Hello, how are you?"
}

Response:
{
  "reply": "AI response here",
  "sessionId": "uuid-here",
  "messageId": 123
}
```

### Analyze Conversation
```
POST /api/chat/analyze
Content-Type: application/json

{
  "sessionId": "uuid-here"
}

Response:
{
  "mistakes": ["Grammar mistake 1", "Grammar mistake 2"],
  "suggestions": ["Suggestion 1", "Suggestion 2"],
  "vocabTips": ["Vocabulary tip 1", "Vocabulary tip 2"]
}
```

### Get Chat History
```
GET /api/chat/history?sessionId=uuid-here

Response: Array of Message objects
[
  {
    "id": 1,
    "sender": "USER",
    "text": "Hello",
    "createdAt": "2024-01-01T10:00:00"
  },
  {
    "id": 2,
    "sender": "AI",
    "text": "Hello! How can I help you?",
    "createdAt": "2024-01-01T10:00:01"
  }
]
```

## Environment Variables

- `GEMINI_API_KEY`: Your Google Gemini API key (required for AI functionality)
- `DATABASE_URL`: PostgreSQL connection URL (production only)
- `DB_USERNAME`: Database username (production only)
- `DB_PASSWORD`: Database password (production only)

## Running the Application

### Development Mode
```bash
# Set environment variable
export GEMINI_API_KEY=your-api-key-here

# Run the application
./mvnw spring-boot:run
```

### Production Mode
```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export GEMINI_API_KEY=your-api-key-here
export DATABASE_URL=jdbc:postgresql://localhost:5432/chatapp
export DB_USERNAME=your-username
export DB_PASSWORD=your-password

# Run the application
./mvnw spring-boot:run
```

## Database Access

In development mode, you can access the H2 console at:
http://localhost:8080/h2-console

- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: password

## CORS Configuration

The application is configured to accept requests from `http://localhost:3000` for frontend integration.

## Future Enhancements

- WebSocket streaming for real-time chat
- User authentication
- Chat room functionality
- File upload support
- Advanced AI prompt customization