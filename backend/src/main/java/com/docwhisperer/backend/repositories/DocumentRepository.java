package com.docwhisperer.backend.repositories;

import com.docwhisperer.backend.documents.Document;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) for the Document entity.
 * Provides standard CRUD operations (Create, Read, Update, Delete)
 * to interact with the underlying database table.
 */
@Repository
public interface DocumentRepository extends CrudRepository<Document, String> {
}
