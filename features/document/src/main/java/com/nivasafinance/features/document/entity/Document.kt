package com.nivasafinance.features.document.entity

import annotations.NoArg
import audit.AuditableEntity
import com.nivasafinance.features.document.enum.ProviderType
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import org.javers.core.metamodel.annotation.TypeName
import java.util.UUID

@Entity
@TypeName("document")
@Table(name = "documents")
@NoArg
@Suppress("LongParameterList")
class Document(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id")
    var documentId: UUID? = null,

    // Core file metadata
    @Column(name = "file_name", nullable = false)
    var fileName: String,

    @Column(name = "file_type")
    var fileType: String? = null,

    @Column(name = "file_size")
    var fileSize: Long? = null,

    // Storage information
    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    var provider: ProviderType,

    @Column(name = "storage_key", nullable = false)
    var storageKey: String,

    @Column(name = "file_url")
    var fileUrl: String? = null,

    // Classification
    @Column(name = "category")
    var category: String? = null,

    @Column(name = "doc_type")
    var docType: String? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    var tags: List<String>? = null,

    // Extra flexible metadata
    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ext_data", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null,

    // Note: Audit fields (createdBy, createdAt, updatedBy, updatedAt, version)
    // are inherited from AuditableEntity

) : AuditableEntity()
