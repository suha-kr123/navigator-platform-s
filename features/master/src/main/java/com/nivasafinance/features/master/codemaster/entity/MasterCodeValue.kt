package com.nivasafinance.features.master.codemaster.entity

import com.nivasafinance.common.audit.AuditableEntity
import com.nivasafinance.common.base.model.MasterLanguageData
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
@Table(name = "master_code_value")
data class MasterCodeValue(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "key", nullable = false, length = 100)
    val key: String,

    @Column(name = "code_key", nullable = false, length = 100)
    val codeKey: String,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", columnDefinition = "jsonb")
    val value: MasterLanguageData? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", columnDefinition = "jsonb")
    val description: MasterLanguageData? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true

) : AuditableEntity()
