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

    fun findByEntityIdAndEntityTypeAndVerificationStatus(
        entityId: UUID,
        entityType: String,
        verificationStatus: com.nivasafinance.features.document.enum.VerificationStatus
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

    @Query("SELECT d FROM Document d WHERE d.entityType = :entityType AND d.verificationStatus = 'VERIFIED'")
    fun findVerifiedDocumentsByEntityType(@Param("entityType") entityType: String): List<Document>
}
