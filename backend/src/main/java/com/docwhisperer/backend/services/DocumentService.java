package com.docwhisperer.backend.services;

import com.docwhisperer.backend.documents.Document;
import com.docwhisperer.backend.repositories.DocumentRepository;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

/**
 * Service class that handles the core business logic for documents.
 * <p>
 * It coordinates the following operations:
 * <ul>
 *     <li>Parsing uploaded files (PDF/DOCX) using Apache Tika.</li>
 *     <li>Splitting text into chunks and generating vector embeddings.</li>
 *     <li>Ingesting data into the Vector Store (PostgreSQL with pgvector).</li>
 *     <li>Managing document metadata in the relational database.</li>
 * </ul>
 * </p>
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final EmbeddingStoreIngestor ingestor;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor injection of dependencies.
     *
     * @param documentRepository The DAO for document metadata.
     * @param embeddingModel     The model used to generate vector embeddings.
     * @param embeddingStore     The vector database for storing embeddings.
     * @param jdbcTemplate       Spring JDBC template for executing direct SQL queries.
     */
    public DocumentService(
            DocumentRepository documentRepository,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            JdbcTemplate jdbcTemplate
    ) {
        this.documentRepository = documentRepository;
        this.jdbcTemplate = jdbcTemplate;
        
        // The Ingestor automatically:
        // 1. Splits text into chunks
        // 2. Converts chunks to vectors (using embeddingModel)
        // 3. Saves vectors to DB (using embeddingStore)
        this.ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 50)) // Split into 500-char chunks
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    /**
     * Processes an uploaded file:
     * <ol>
     *     <li>Parses text from PDF/DOCX using Apache Tika.</li>
     *     <li>Ingests text into Vector DB (splits, embeds, stores).</li>
     *     <li>Saves metadata to Postgres 'document' table.</li>
     * </ol>
     *
     * @param file The uploaded file.
     * @return The saved Document metadata.
     * @throws IOException If file reading fails.
     */
    public Document store(MultipartFile file) throws IOException {
        log.info("Processing upload for file: {}", file.getOriginalFilename());

        // 1. Parse Document using Tika
        var parser = new ApacheTikaDocumentParser();
        var document = parser.parse(file.getInputStream());
        log.info("Parsed text length: {} chars", document.text().length());
        
        // 2. Assign metadata ID to link vectors to this document
        String docId = UUID.randomUUID().toString();
        document.metadata().put("documentId", docId);
        
        // 3. Ingest (Split -> Embed -> Store Vectors)
        log.info("Starting ingestion into vector store...");
        ingestor.ingest(document);
        log.info("Ingestion completed for documentId: {}", docId);

        // 4. Save Metadata to DB
        Document docEntity = new Document(
                docId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                LocalDateTime.now(),
                1 // Simple placeholder, Tika can extract real page count if needed
        );
        
        return documentRepository.save(docEntity);
    }

    /**
     * Retrieves all documents from the metadata database.
     * @return List of all documents.
     */
    public List<Document> getAllDocuments() {
        return StreamSupport.stream(documentRepository.findAll().spliterator(), false).toList();
    }
    
    /**
     * Deletes a document by ID.
     * <p>
     * This method performs a cascading delete:
     * 1. Removes all vector embeddings associated with the document ID from the 'embeddings' table.
     * 2. Removes the document metadata from the 'document' table.
     * </p>
     *
     * @param id The document ID to delete.
     */
    public void deleteDocument(String id) {
        // 1. Delete vectors from Embedding Store (Vector DB)
        // Using direct SQL because EmbeddingStore interface might not expose filter deletion in this version
        String deleteSql = "DELETE FROM embeddings WHERE metadata ->> 'documentId' = ?";
        jdbcTemplate.update(deleteSql, id);
        
        // 2. Delete metadata from Relational DB
        documentRepository.deleteById(id);
    }
}
