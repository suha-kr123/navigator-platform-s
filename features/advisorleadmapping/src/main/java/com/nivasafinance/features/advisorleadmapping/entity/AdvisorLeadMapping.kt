package com.nivasafinance.features.advisorleadmapping.entity

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
@Table(name = "advisor_lead_mapping")
@NoArg
@Suppress("LongParameterList")
data class AdvisorLeadMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "advisor_id")
    var advisorId: UUID? = null,

    @Column(name = "lead_id")
    var leadId: UUID? = null,

    @Column(name = "verification_status", length = 50)
    var verificationStatus: String? = null,

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    var verificationNotes: String? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_ids", columnDefinition = "jsonb", nullable = true)
    var paymentIds: List<UUID>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null
) : AuditableEntity()
