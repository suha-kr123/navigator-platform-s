package com.nivasafinance.features.identifiers.entity

import annotations.NoArg
import audit.AuditableEntity
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "identifiers")
@NoArg
@Suppress("LongParameterList")
class Identifier(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "entity_id", nullable = false)
    var entityId: UUID? = null,

    @Column(name = "entity_type", nullable = false)
    var entityType: String? = null,

    @Column(name = "identifier", nullable = false, length = 100)
    var identifier: String? = null,

    @Column(name = "type", nullable = false, length = 100)
    var type: String? = null,

    @Column(name = "is_primary")
    var isPrimary: Boolean = false,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null

) : AuditableEntity()
