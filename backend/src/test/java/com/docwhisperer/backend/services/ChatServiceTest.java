package com.docwhisperer.backend.services;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatLanguageModel chatLanguageModel;

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Captor
    private ArgumentCaptor<Object[]> paramsCaptor;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(chatLanguageModel, embeddingModel, jdbcTemplate);
    }

    @Test
    @DisplayName("Should return answer when relevant segments are found")
    void answer_withRelevantSegments_returnsLlmResponse() {
        // Arrange
        String question = "What is the main topic?";
        String documentId = "doc-123";
        String expectedResponse = "The main topic is Java programming.";

        // Mock embedding count check
        when(jdbcTemplate.queryForObject(
                contains("SELECT COUNT(*)"),
                eq(Integer.class),
                eq(documentId)
        )).thenReturn(5);

        // Mock embedding model
        float[] mockVector = new float[]{0.1f, 0.2f, 0.3f};
        Embedding mockEmbedding = Embedding.from(mockVector);
        when(embeddingModel.embed(question)).thenReturn(new Response<>(mockEmbedding));

        // Mock vector search results
        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<String>>any(),
                any(Object[].class)
        )).thenReturn(List.of("Segment 1: Java basics", "Segment 2: Advanced topics"));

        // Mock LLM response - LangChain4j 0.30.0 uses Response<AiMessage>
        AiMessage aiMessage = AiMessage.from(expectedResponse);
        Response<AiMessage> llmResponse = new Response<>(aiMessage);
        when(chatLanguageModel.generate(any(ChatMessage.class), any(ChatMessage.class)))
                .thenReturn(llmResponse);

        // Act
        String result = chatService.answer(question, documentId);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        verify(embeddingModel).embed(question);
        verify(chatLanguageModel).generate(any(ChatMessage.class), any(ChatMessage.class));
    }

    @Test
    @DisplayName("Should return error message when no embeddings exist for document")
    void answer_withNoEmbeddings_returnsErrorMessage() {
        // Arrange
        String question = "What is the content?";
        String documentId = "empty-doc";

        when(jdbcTemplate.queryForObject(
                contains("SELECT COUNT(*)"),
                eq(Integer.class),
                eq(documentId)
        )).thenReturn(0);

        // Act
        String result = chatService.answer(question, documentId);

        // Assert
        assertThat(result).contains("document seems to be empty");
        verify(embeddingModel, never()).embed(anyString());
        verify(chatLanguageModel, never()).generate(any(ChatMessage.class), any(ChatMessage.class));
    }

    @Test
    @DisplayName("Should return error message when no relevant segments found")
    void answer_withNoRelevantSegments_returnsNotFoundMessage() {
        // Arrange
        String question = "Unrelated question?";
        String documentId = "doc-456";

        when(jdbcTemplate.queryForObject(
                contains("SELECT COUNT(*)"),
                eq(Integer.class),
                eq(documentId)
        )).thenReturn(10);

        float[] mockVector = new float[]{0.1f, 0.2f, 0.3f};
        when(embeddingModel.embed(question)).thenReturn(new Response<>(Embedding.from(mockVector)));

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<String>>any(),
                any(Object[].class)
        )).thenReturn(Collections.emptyList());

        // Act
        String result = chatService.answer(question, documentId);

        // Assert
        assertThat(result).contains("couldn't find any relevant information");
        verify(chatLanguageModel, never()).generate(any(ChatMessage.class), any(ChatMessage.class));
    }

    @Test
    @DisplayName("Should search all documents when documentId is null")
    void answer_withNullDocumentId_searchesAllDocuments() {
        // Arrange
        String question = "General question?";

        float[] mockVector = new float[]{0.1f, 0.2f, 0.3f};
        when(embeddingModel.embed(question)).thenReturn(new Response<>(Embedding.from(mockVector)));

        when(jdbcTemplate.query(
                sqlCaptor.capture(),
                ArgumentMatchers.<RowMapper<String>>any(),
                paramsCaptor.capture()
        )).thenReturn(List.of("Some content"));

        AiMessage aiMessage = AiMessage.from("Answer");
        Response<AiMessage> llmResponse = new Response<>(aiMessage);
        when(chatLanguageModel.generate(any(ChatMessage.class), any(ChatMessage.class)))
                .thenReturn(llmResponse);

        // Act
        chatService.answer(question, null);

        // Assert
        String executedSql = sqlCaptor.getValue();
        assertThat(executedSql).doesNotContain("metadata ->> 'documentId' = ?");

        // Verify no COUNT query was executed (diagnostic check skipped)
        verify(jdbcTemplate, never()).queryForObject(contains("COUNT"), eq(Integer.class), anyString());
    }

    @Test
    @DisplayName("Should include document filter in SQL when documentId is provided")
    void answer_withDocumentId_includesFilterInQuery() {
        // Arrange
        String question = "Specific question?";
        String documentId = "doc-789";

        when(jdbcTemplate.queryForObject(
                contains("SELECT COUNT(*)"),
                eq(Integer.class),
                eq(documentId)
        )).thenReturn(3);

        float[] mockVector = new float[]{0.1f, 0.2f, 0.3f};
        when(embeddingModel.embed(question)).thenReturn(new Response<>(Embedding.from(mockVector)));

        when(jdbcTemplate.query(
                sqlCaptor.capture(),
                ArgumentMatchers.<RowMapper<String>>any(),
                paramsCaptor.capture()
        )).thenReturn(List.of("Filtered content"));

        AiMessage aiMessage = AiMessage.from("Filtered answer");
        Response<AiMessage> llmResponse = new Response<>(aiMessage);
        when(chatLanguageModel.generate(any(ChatMessage.class), any(ChatMessage.class)))
                .thenReturn(llmResponse);

        // Act
        chatService.answer(question, documentId);

        // Assert
        String executedSql = sqlCaptor.getValue();
        assertThat(executedSql).contains("metadata ->> 'documentId' = ?");

        Object[] params = paramsCaptor.getValue();
        assertThat(params).contains(documentId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when embedding model fails")
    void answer_whenEmbeddingFails_throwsException() {
        // Arrange
        String question = "Test question";
        String documentId = "doc-123";

        when(jdbcTemplate.queryForObject(
                contains("SELECT COUNT(*)"),
                eq(Integer.class),
                eq(documentId)
        )).thenReturn(1);

        when(embeddingModel.embed(question)).thenThrow(new RuntimeException("Embedding service unavailable"));

        // Act & Assert
        assertThatThrownBy(() -> chatService.answer(question, documentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate answer");
    }

    @Test
    @DisplayName("Should construct proper context from multiple segments")
    void answer_withMultipleSegments_constructsContextCorrectly() {
        // Arrange
        String question = "What are the topics?";
        String documentId = "doc-multi";
        List<String> segments = List.of("Topic 1: Introduction", "Topic 2: Methods", "Topic 3: Results");

        when(jdbcTemplate.queryForObject(
                contains("SELECT COUNT(*)"),
                eq(Integer.class),
                eq(documentId)
        )).thenReturn(3);

        float[] mockVector = new float[]{0.1f, 0.2f};
        when(embeddingModel.embed(question)).thenReturn(new Response<>(Embedding.from(mockVector)));

        when(jdbcTemplate.query(
                anyString(),
                ArgumentMatchers.<RowMapper<String>>any(),
                any(Object[].class)
        )).thenReturn(segments);

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        AiMessage aiMessage = AiMessage.from("Combined answer");
        Response<AiMessage> llmResponse = new Response<>(aiMessage);
        when(chatLanguageModel.generate(messageCaptor.capture(), any(ChatMessage.class)))
                .thenReturn(llmResponse);

        // Act
        chatService.answer(question, documentId);

        // Assert
        ChatMessage systemMessage = messageCaptor.getValue();
        String systemText = systemMessage.toString();
        assertThat(systemText).contains("Topic 1: Introduction");
        assertThat(systemText).contains("Topic 2: Methods");
        assertThat(systemText).contains("Topic 3: Results");
    }
}
