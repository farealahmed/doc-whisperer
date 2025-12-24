package com.docwhisperer.backend.services;

import dev.langchain4j.service.SystemMessage;

/**
 * The AI Assistant interface.
 * <p>
 * This interface defines the contract for interacting with the LLM.
 * LangChain4j uses the "AI Service" pattern to automatically implement this interface at runtime,
 * wiring it up with the configured ChatLanguageModel, ContentRetriever (RAG), and ChatMemory.
 * </p>
 */
public interface ChatAgent {

    /**
     * Generates an answer to the user's question.
     * <p>
     * The {@code @SystemMessage} annotation defines the system prompt that instructs the LLM on its role and behavior.
     * The RAG system will automatically inject relevant document chunks into the context before calling this method.
     * </p>
     *
     * @param question The user's question.
     * @return The AI's response.
     */
    @SystemMessage("You are a helpful document assistant. Answer the user's question based ONLY on the provided document context. If you don't know the answer, say so.")
    String answer(String question);
}