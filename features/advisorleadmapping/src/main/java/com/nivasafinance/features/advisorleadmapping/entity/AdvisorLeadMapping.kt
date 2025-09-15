package com.nivasafinance.features.advisorleadmapping.entity

import annotations.NoArg
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
@Table(name = "advisor_lead_mapping")
@NoArg
@Suppress("LongParameterList")
class AdvisorLeadMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "advisor_id", nullable = false)
    val advisorId: UUID,

    @Column(name = "lead_id", nullable = false)
    val leadId: UUID,

    @Column(name = "remarks")
    val remarks: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    val extData: Map<String, Any>? = null
) : AuditableEntity()
