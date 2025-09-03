package com.nivasafinance.features.document.repository

import com.nivasafinance.features.document.entity.Document
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface DocumentRepository : JpaRepository<Document, UUID> {

    // Basic CRUD operations - only these are needed
    // save() and delete() are inherited from JpaRepository
    // findById() is inherited from JpaRepository and returns Optional<Document>
}
