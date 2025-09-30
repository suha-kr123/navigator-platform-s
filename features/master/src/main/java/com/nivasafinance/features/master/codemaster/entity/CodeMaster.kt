package com.nivasafinance.features.master.codemaster.entity

import audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "code_master")
data class CodeMaster(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "code_name", nullable = false, length = 100)
    val codeName: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "code_value", columnDefinition = "jsonb", nullable = false)
    val codeValue: CodeValue

) : AuditableEntity()

data class CodeValue(
    val id: Int,
    val key: String,
    val value: Map<String, String>
)
