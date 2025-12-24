package com.docwhisperer.backend.controllers;

/*
Why it is needed: To expose an API endpoint that the React frontend can call to ask questions.

What it will do:

- POST /api/chat :
  - Accepts a JSON body: { "question": "..." } .
  - Calls chatAgent.answer(question) .
  - Returns the AI's response string. */

import com.docwhisperer.backend.services.ChatAgent;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
/**
 * REST Controller for handling chat interactions.
 * <p>
 * This controller exposes endpoints to interact with the AI assistant.
 * It acts as a bridge between the frontend React application and the backend {@link ChatAgent}.
 * </p>
 */
public class ChatController {

    private final ChatAgent chatAgent;

    /**
     * Constructor injection for the ChatAgent service.
     * @param chatAgent The AI service responsible for generating answers.
     */
    public ChatController(ChatAgent chatAgent) {
        this.chatAgent = chatAgent;
    }

    /**
     * Processes a chat message from the user.
     * <p>
     * This endpoint receives a user's question, sends it to the RAG pipeline (ChatAgent),
     * and returns the AI's response.
     * </p>
     *
     * @param payload A map containing the "question" key.
     * @return A map containing the "answer" key with the AI's response.
     * @throws IllegalArgumentException if the question is missing or empty.
     */
    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        if (question == null || question.trim().isEmpty()) {
             throw new IllegalArgumentException("Question cannot be empty");
        }
        
        String answer = chatAgent.answer(question);
        return Map.of("answer", answer);
    }
}
