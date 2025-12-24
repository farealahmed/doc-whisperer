package com.docwhisperer.backend.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * Configuration class for the RAG (Retrieval-Augmented Generation) pipeline.
 * <p>
 * This class defines the necessary Spring beans to set up LangChain4j, including:
 * <ul>
 *     <li>{@link EmbeddingModel}: Converts text into vector embeddings.</li>
 *     <li>{@link EmbeddingStore}: Stores and retrieves vector embeddings (PostgreSQL).</li>
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
     * Creates the Vector Store bean connected to PostgreSQL (pgvector).
     * <p>
     * CRITICAL: We must explicitly configure the host and PORT (5433) because
     * the auto-configuration defaults to 5432, but our Docker container is mapped to 5433.
     * </p>
     */
    @Bean
    EmbeddingStore<TextSegment> embeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5433)
                .database("docwhisperer")
                .user("postgres")
                .password("password")
                .table("embeddings")
                .dimension(384)
                .build();
    }
}