package com.nivasafinance.features.lead.entity

import annotations.NoArg
import audit.AuditableEntity
import com.nivasafinance.features.lead.dto.LeadContacts
import com.nivasafinance.features.lead.dto.LeadPreliminaryInformation
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "leads")
@NoArg
@Suppress("LongParameterList")
data class Lead(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "requested_amount", nullable = false)
    var requestedAmount: BigDecimal?,

    @Column(name = "purpose", length = 40, nullable = false)
    var purpose: String?,

    @Column(name = "product_code", nullable = false)
    var productCode: String?,

    @Column(name = "pipeline_key", nullable = false)
    var pipelineKey: String?,

    @Column(name = "current_stage", nullable = false)
    var currentStage: String?,

    @Column(name = "sourcing_channel")
    var sourcingChannel: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preliminary_information", columnDefinition = "jsonb")
    var preliminaryInformation: Map<String, Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ext_data", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null
    
) : AuditableEntity()
