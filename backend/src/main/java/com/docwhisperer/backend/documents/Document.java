package com.docwhisperer.backend.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

/**
 * Represents the metadata of an uploaded document.
 * This entity is stored in the database to keep track of user files.
 *
 * @param id Unique identifier (UUID string)
 * @param name Original filename (e.g., "report.pdf")
 * @param type MIME type or extension (e.g., "application/pdf")
 * @param size File size in bytes
 * @param uploadedAt Timestamp when the file was uploaded
 * @param pageCount Number of pages extracted from the document
 */
@Table("document")
public record Document(
    @Id String id,
    String name,
    String type,
    Long size,
    LocalDateTime uploadedAt,
    Integer pageCount
) {}
