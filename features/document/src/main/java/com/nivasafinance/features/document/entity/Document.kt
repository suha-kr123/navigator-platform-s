package com.nivasafinance.features.document.entity

import com.nivasafinance.common.annotations.NoArg
import com.nivasafinance.common.audit.AuditableEntity
import com.nivasafinance.features.document.enum.DocumentStorageProvider
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
@Table(name = "n_documents")
@NoArg
@Suppress("LongParameterList")
class Document(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    // application/pdf
    @Column(name = "type")
    var type: String? = null,

    // in kB
    @Column(name = "size")
    var size: Long? = null,

    // Storage information
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    var provider: DocumentStorageProvider,

    @Column(name = "path", nullable = false)
    var path: String,

    // Extra flexible metadata
    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null,
) : AuditableEntity()
