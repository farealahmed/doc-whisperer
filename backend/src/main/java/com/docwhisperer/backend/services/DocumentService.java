package com.docwhisperer.backend.services;

import com.docwhisperer.backend.documents.Document;
import com.docwhisperer.backend.repositories.DocumentRepository;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

/**
 * Service class that handles the core business logic for documents.
 * It coordinates file parsing, vector embedding, and database storage.
 */
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final EmbeddingStoreIngestor ingestor;

    // Constructor injection of dependencies
    public DocumentService(
            DocumentRepository documentRepository,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore
    ) {
        this.documentRepository = documentRepository;
        
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
     * 1. Parses text from PDF/DOCX using Apache Tika.
     * 2. Ingests text into Vector DB.
     * 3. Saves metadata to Postgres.
     */
    public Document store(MultipartFile file) throws IOException {
        // 1. Parse Document using Tika
        var parser = new ApacheTikaDocumentParser();
        var document = parser.parse(file.getInputStream());
        
        // 2. Assign metadata ID to link vectors to this document
        String docId = UUID.randomUUID().toString();
        document.metadata().put("documentId", docId);
        
        // 3. Ingest (Split -> Embed -> Store Vectors)
        ingestor.ingest(document);

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

    public List<Document> getAllDocuments() {
        return StreamSupport.stream(documentRepository.findAll().spliterator(), false).toList();
    }
    
    public void deleteDocument(String id) {
        // TODO: Also delete vectors from embedding store (requires advanced filter support)
        documentRepository.deleteById(id);
    }
}
