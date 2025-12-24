package com.docwhisperer.backend.controllers;

/*
Why it is needed: To expose an API endpoint that the React frontend can call to ask questions.

What it will do:

- POST /api/chat :
  - Accepts a JSON body: { "question": "..." } .
  - Calls chatAgent.answer(question) .
  - Returns the AI's response string. */

import com.docwhisperer.backend.services.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
/**
 * REST Controller for handling chat interactions.
 * <p>
 * This controller exposes endpoints to interact with the AI assistant.
 * It acts as a bridge between the frontend React application and the backend {@link ChatService}.
 * </p>
 */
public class ChatController {

    private final ChatService chatService;

    /**
     * Constructor injection for the ChatService.
     * @param chatService The AI service responsible for generating answers.
     */
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Processes a chat message from the user.
     * <p>
     * This endpoint receives a user's question, sends it to the RAG pipeline (ChatService),
     * and returns the AI's response.
     * </p>
     *
     * @param payload A map containing the "question" key and optional "documentId".
     * @return A map containing the "answer" key with the AI's response.
     * @throws IllegalArgumentException if the question is missing or empty.
     */
    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        if (question == null || question.trim().isEmpty()) {
             throw new IllegalArgumentException("Question cannot be empty");
        }
        
        String documentId = payload.get("documentId");
        
        String answer = chatService.answer(question, documentId);
        return Map.of("answer", answer);
    }
}
