package com.nivasafinance.features.document.repository;

import com.nivasafinance.features.document.entity.Document;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByIdentifier(UUID identifier);
}

