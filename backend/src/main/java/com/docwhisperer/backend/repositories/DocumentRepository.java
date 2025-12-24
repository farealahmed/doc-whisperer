package com.docwhisperer.backend.repositories;

import com.docwhisperer.backend.documents.Document;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) for the Document entity.
 * <p>
 * Extends {@link CrudRepository} to provide standard CRUD operations (Create, Read, Update, Delete)
 * to interact with the underlying 'document' table in PostgreSQL.
 * Spring Data JDBC automatically implements this interface at runtime.
 * </p>
 */
@Repository
public interface DocumentRepository extends CrudRepository<Document, String> {
}
