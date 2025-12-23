package com.docwhisperer.backend.config;

import com.docwhisperer.backend.services.ChatAgent;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfiguration {

    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    ChatAgent chatAgent(ChatLanguageModel chatLanguageModel, 
                        EmbeddingStore<TextSegment> embeddingStore, 
                        EmbeddingModel embeddingModel) {
        
        // 1. Define how to find relevant information
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3) // Retrieve top 3 most relevant chunks
                .minScore(0.6) // Only retrieve chunks with >60% similarity
                .build();

        // 2. Build the AI Service
        return AiServices.builder(ChatAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // Remember last 10 messages
                .build();
    }
}