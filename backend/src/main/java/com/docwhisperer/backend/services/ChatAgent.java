package com.docwhisperer.backend.services;

import dev.langchain4j.service.SystemMessage;

/**
 * The AI Assistant interface.
 * LangChain4j will automatically implement this interface at runtime.
 */
public interface ChatAgent {

    @SystemMessage("You are a helpful document assistant. Answer the user's question based ONLY on the provided document context. If you don't know the answer, say so.")
    String answer(String question);
}