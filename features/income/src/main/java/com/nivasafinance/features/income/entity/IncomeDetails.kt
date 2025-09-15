package com.nivasafinance.features.income.entity

import annotations.NoArg
import audit.AuditableEntity
import com.nivasafinance.features.income.enum.EmployerType
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
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "income_details")
@NoArg
@Suppress("LongParameterList")
class IncomeDetails(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "employment_id")
    var employmentId: UUID? = null,

    @Column(name = "entity_id", nullable = false)
    var entityId: UUID? = null,

    @Column(name = "entity_type", nullable = false)
    var entityType: String? = null,

    @Column(name = "employment_type", nullable = false)
    var employmentType: String? = null,

    @Column(name = "employer_name")
    var employerName: String? = null,

    @Column(name = "employer_type")
    @Enumerated(EnumType.STRING)
    var employerType: EmployerType? = null,

    @Column(name = "job_title")
    var jobTitle: String? = null,

    @Column(name = "department")
    var department: String? = null,

    @Column(name = "location")
    var location: String? = null,

    @Column(name = "salary", precision = 12, scale = 2)
    var salary: BigDecimal? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "documents", columnDefinition = "jsonb")
    var documents: Map<String, Any>? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null

) : AuditableEntity()
