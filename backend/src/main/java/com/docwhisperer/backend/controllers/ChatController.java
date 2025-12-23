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
public class ChatController {

    private final ChatAgent chatAgent;

    public ChatController(ChatAgent chatAgent) {
        this.chatAgent = chatAgent;
    }

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
