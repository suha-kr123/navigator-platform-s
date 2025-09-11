package com.nivasafinance.features.document.repository

import com.nivasafinance.features.document.entity.Document
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface DocumentRepository : JpaRepository<Document, UUID> {

    fun findByEntityIdAndEntityType(entityId: UUID, entityType: String): List<Document>

    fun findByEntityIdAndEntityTypeAndDocumentType(
        entityId: UUID,
        entityType: String,
        documentType: String
    ): List<Document>

    fun findByEntityIdAndEntityTypeAndIsRequired(
        entityId: UUID,
        entityType: String,
        isRequired: Boolean
    ): List<Document>

    fun findByEntityIdAndEntityTypeAndIsVerified(
        entityId: UUID,
        entityType: String,
        isVerified: Boolean
    ): List<Document>

    fun existsByEntityIdAndEntityTypeAndDocumentType(
        entityId: UUID,
        entityType: String,
        documentType: String
    ): Boolean

    @Query("SELECT d FROM Document d WHERE d.entityType = :entityType AND d.documentType = :documentType")
    fun findByEntityTypeAndDocumentType(
        @Param("entityType") entityType: String,
        @Param("documentType") documentType: String
    ): List<Document>

    @Query("SELECT d FROM Document d WHERE d.entityType = :entityType AND d.isRequired = true")
    fun findRequiredDocumentsByEntityType(@Param("entityType") entityType: String): List<Document>
}
