package com.docwhisperer.backend.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Represents the metadata of an uploaded document.
 * This entity is stored in the database (PostgreSQL) to keep track of user files.
 * <p>
 * It implements {@link Persistable} to control the "new" state manually.
 * This is required because we generate the ID (UUID) before saving, and by default,
 * Spring Data JDBC assumes an entity with an ID is not new (triggering an update instead of insert).
 * </p>
 */
@Table("document")
public class Document implements Persistable<String> {

    /**
     * Unique identifier for the document (UUID).
     */
    @Id
    private String id;
    private String name;
    private String type;
    private Long size;
    private LocalDateTime uploadedAt;
    private Integer pageCount;

    /**
     * Transient flag to indicate if the entity is new.
     * Not stored in the database.
     */
    @Transient
    private boolean isNew = false;

    // Default constructor for Spring Data
    public Document() {
    }

    // Constructor for creating new documents
    public Document(String id, String name, String type, Long size, LocalDateTime uploadedAt, Integer pageCount) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.size = size;
        this.uploadedAt = uploadedAt;
        this.pageCount = pageCount;
        this.isNew = true; // Mark as new for insertion
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public void setId(String id) { this.id = id; }
}
