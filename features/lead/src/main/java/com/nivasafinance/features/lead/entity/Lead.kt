package com.nivasafinance.features.lead.entity

import audit.AuditableEntity
import com.nivasafinance.features.lead.dto.LeadContacts
import com.nivasafinance.features.lead.dto.LeadPreliminaryInformation
import com.nivasafinance.features.lead.enum.LeadStage
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
data class Lead(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    var requestedAmount: BigDecimal?,

    @Column(length = 40, nullable = false)
    var purpose: String?,

    @Column(name = "product_code", nullable = false)
    var productCode: String?,

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    var status: LeadStatus?,

    @Column(length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    var stage: LeadStage?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preliminary_information", columnDefinition = "jsonb")
    var preliminaryInformation: LeadPreliminaryInformation?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lead_contacts", columnDefinition = "jsonb")
    var leadContacts: LeadContacts?,

    @Column(name = "sourcing_channel", length = 50)
    var sourcingChannel: SourcingChannel?
) : AuditableEntity()
