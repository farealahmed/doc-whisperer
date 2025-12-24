package com.docwhisperer.backend.controllers;

import com.docwhisperer.backend.documents.Document;
import com.docwhisperer.backend.services.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*") // Allow requests from any frontend (React, curl, etc.)
/**
 * REST Controller for managing documents.
 * <p>
 * Provides endpoints for uploading, listing, and deleting documents.
 * It interacts with the {@link DocumentService} to perform business logic.
 * </p>
 */
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Uploads a new document file (PDF or DOCX).
     * <p>
     * This endpoint accepts a multipart file upload, processes it (text extraction, vectorization),
     * and saves the metadata to the database.
     * </p>
     *
     * @param file The file uploaded by the user.
     * @return The saved Document metadata.
     * @throws IOException If an error occurs during file processing.
     */
    @PostMapping
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Document savedDoc = documentService.store(file);
        return ResponseEntity.ok(savedDoc);
    }

    /**
     * Retrieves a list of all uploaded documents.
     *
     * @return A list of Document objects containing metadata (name, size, type, etc.).
     */
    @GetMapping
    public List<Document> listDocuments() {
        return documentService.getAllDocuments();
    }

    /**
     * Deletes a document by its ID.
     * <p>
     * This removes both the document metadata from the relational database
     * and the associated vector embeddings from the vector store.
     * </p>
     *
     * @param id The unique identifier of the document to delete.
     * @return HTTP 204 No Content if successful.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}