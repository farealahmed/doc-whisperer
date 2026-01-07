package com.docwhisperer.backend.services;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for handling chat interactions with document context (RAG).
 * <p>
 * This service uses direct SQL queries for vector similarity search with proper
 * document filtering, bypassing potential issues with LangChain4j's filter API.
 * </p>
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatLanguageModel chatLanguageModel;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    public ChatService(ChatLanguageModel chatLanguageModel,
                       EmbeddingModel embeddingModel,
                       JdbcTemplate jdbcTemplate) {
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Answers a user's question using RAG (Retrieval-Augmented Generation).
     *
     * @param question   The user's question.
     * @param documentId The ID of the document to scope the search to (optional).
     * @return The AI's response.
     */
    public String answer(String question, String documentId) {
        log.info("Received question: '{}' for documentId: '{}'", question, documentId);

        try {
            // Diagnostic check: Verify if embeddings exist for this document
            if (documentId != null) {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM embeddings WHERE metadata ->> 'documentId' = ?",
                    Integer.class,
                    documentId
                );
                log.info("Diagnostic: Found {} existing embeddings for documentId: {}", count, documentId);

                if (count != null && count == 0) {
                    log.warn("Diagnostic: No embeddings found for this document! It might have been uploaded incorrectly.");
                    return "I apologize, but this document seems to be empty or was not processed correctly. Please try deleting and re-uploading it.";
                }
            }

            // 1. Embed the user's question
            Embedding questionEmbedding = embeddingModel.embed(question).content();
            String embeddingVector = embeddingToString(questionEmbedding);

            // 2. Search using direct SQL with proper filtering
            // Using low minScore (0.0) since cosine similarity scores vary widely
            List<String> relevantTexts = searchWithFilter(embeddingVector, documentId, 5, 0.0);
            log.info("Found {} relevant segments", relevantTexts.size());

            // 3. Handle case with no relevant information
            if (relevantTexts.isEmpty()) {
                log.warn("No relevant segments found for question: '{}'", question);
                return "I apologize, but I couldn't find any relevant information in this document to answer your question. " +
                       "The document might be empty or the content might not be indexable.";
            }

            // 4. Construct Context from retrieved segments
            String context = String.join("\n\n", relevantTexts);

            // 5. Create Prompt with Context
            String systemPrompt = "You are a helpful document assistant. Answer the user's question based ONLY on the provided context below. " +
                    "If the context doesn't contain the answer, say so.\n\n" +
                    "Context:\n" + context;

            // 6. Generate Response
            log.info("Sending request to LLM...");
            String response = chatLanguageModel.generate(
                    SystemMessage.from(systemPrompt),
                    UserMessage.from(question)
            ).content().text();
            log.info("Received response from LLM");

            return response;

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            throw new RuntimeException("Failed to generate answer", e);
        }
    }

    /**
     * Performs vector similarity search with document filtering using direct SQL.
     * This bypasses potential issues with LangChain4j's filter API in version 0.30.0.
     */
    private List<String> searchWithFilter(String embeddingVector, String documentId, int maxResults, double minScore) {
        String sql;
        Object[] params;

        if (documentId != null && !documentId.trim().isEmpty()) {
            // Query WITH document filter - only search within the specific document
            sql = """
                SELECT text, 1 - (embedding <=> ?::vector) as score, metadata ->> 'documentId' as doc_id
                FROM embeddings
                WHERE metadata ->> 'documentId' = ?
                AND 1 - (embedding <=> ?::vector) >= ?
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;
            params = new Object[]{embeddingVector, documentId, embeddingVector, minScore, embeddingVector, maxResults};
        } else {
            // Query WITHOUT filter - search all documents
            sql = """
                SELECT text, 1 - (embedding <=> ?::vector) as score, metadata ->> 'documentId' as doc_id
                FROM embeddings
                WHERE 1 - (embedding <=> ?::vector) >= ?
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;
            params = new Object[]{embeddingVector, embeddingVector, minScore, embeddingVector, maxResults};
        }

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String text = rs.getString("text");
            double score = rs.getDouble("score");
            String docId = rs.getString("doc_id");
            log.info("Retrieved chunk from documentId: {}, score: {}, text preview: {}...",
                    docId, score, text.substring(0, Math.min(50, text.length())));
            return text;
        }, params);
    }

    /**
     * Converts a LangChain4j Embedding to PostgreSQL vector string format.
     */
    private String embeddingToString(Embedding embedding) {
        float[] vector = embedding.vector();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
