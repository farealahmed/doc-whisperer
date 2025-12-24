package com.docwhisperer.backend.services;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
import static java.util.stream.Collectors.joining;

/**
 * Service for handling chat interactions with document context (RAG).
 * <p>
 * This service replaces the auto-generated ChatAgent to provide finer control
 * over the retrieval process, specifically to support filtering by document ID.
 * </p>
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatLanguageModel chatLanguageModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    public ChatService(ChatLanguageModel chatLanguageModel,
                       EmbeddingStore<TextSegment> embeddingStore,
                       EmbeddingModel embeddingModel,
                       JdbcTemplate jdbcTemplate) {
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingStore = embeddingStore;
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
            var questionEmbedding = embeddingModel.embed(question).content();

            // 2. Build Filter (if documentId is provided)
            Filter filter = null;
            if (documentId != null && !documentId.trim().isEmpty()) {
                filter = metadataKey("documentId").isEqualTo(documentId);
            }

            // 3. Search Embedding Store with Filter
            // Lowered minScore to 0.5 to be more inclusive for broad queries like "summarize"
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(questionEmbedding)
                    .filter(filter)
                    .minScore(0.5) 
                    .maxResults(5) 
                    .build();

            List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(request).matches();
            log.info("Found {} relevant segments", relevant.size());

            // 4. Handle case with no relevant information
            if (relevant.isEmpty()) {
                log.warn("No relevant segments found for question: '{}'", question);
                return "I apologize, but I couldn't find any relevant information in this document to answer your question. " +
                       "The document might be empty or the content might not be indexable.";
            }

            // 5. Construct Context from retrieved segments
            String context = relevant.stream()
                    .map(match -> match.embedded().text())
                    .collect(joining("\n\n"));

            // 6. Create Prompt with Context
            String systemPrompt = "You are a helpful document assistant. Answer the user's question based ONLY on the provided context below. " +
                    "If the context doesn't contain the answer, say so.\n\n" +
                    "Context:\n" + context;

            // 7. Generate Response
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
}
