package com.nivasafinance.features.master.codemaster.entity

import audit.AuditableEntity
import base.model.MasterLanguageData
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
@Table(name = "master_code")
data class MasterCode(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "parent_id")
    val parentId: UUID? = null,

    @Column(name = "key", nullable = false, length = 100)
    val key: String,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "name", columnDefinition = "jsonb")
    val name: MasterLanguageData? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", columnDefinition = "jsonb")
    val description: MasterLanguageData? = null,

    @Column(name = "is_system_defined", nullable = false)
    val isSystemDefined: Boolean = false

) : AuditableEntity()
