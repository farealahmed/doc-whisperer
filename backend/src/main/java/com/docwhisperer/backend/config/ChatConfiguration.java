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
/**
 * Configuration class for the RAG (Retrieval-Augmented Generation) pipeline.
 * <p>
 * This class defines the necessary Spring beans to set up LangChain4j, including:
 * <ul>
 *     <li>{@link EmbeddingModel}: Converts text into vector embeddings.</li>
 *     <li>{@link ChatAgent}: The AI service interface that interacts with the LLM.</li>
 *     <li>{@link ContentRetriever}: Logic for finding relevant document chunks in the vector store.</li>
 * </ul>
 * </p>
 */
public class ChatConfiguration {

    /**
     * Creates an EmbeddingModel bean using the "AllMiniLmL6V2" model.
     * This model is used to compute vector embeddings for both document chunks and user queries.
     * It runs locally within the Java process (ONNX runtime).
     *
     * @return The configured EmbeddingModel.
     */
    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * Creates the ChatAgent AI Service.
     * <p>
     * This bean wires together the Language Model (Ollama), the Vector Store (PostgreSQL/pgvector),
     * and the Embedding Model to create a complete RAG system.
     * </p>
     *
     * @param chatLanguageModel The LLM backend (configured via application.properties to use Ollama).
     * @param embeddingStore    The vector database where document chunks are stored.
     * @param embeddingModel    The model used to vectorize queries.
     * @return A proxy instance of ChatAgent that handles the AI interaction logic.
     */
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