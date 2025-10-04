package com.nivasafinance.features.leadlender.entity

import com.nivasafinance.common.audit.AuditableEntity
import com.nivasafinance.features.leadlender.dto.RmDetails
import com.nivasafinance.features.leadlender.enum.LeadLenderStatus
import com.nivasafinance.features.leadlender.enum.RejectReason
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
import java.util.UUID

@Entity
@Table(name = "lead_lender")
data class LeadLender(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "lead_id", nullable = false)
    val leadId: UUID,

    @Column(name = "lender_key", nullable = false, length = 20)
    val lenderKey: String,

    @Column(name = "status", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    var status: LeadLenderStatus,

    @Column(name = "lender_office_key", length = 20)
    var lenderOfficeKey: String? = null,

    @Column(name = "rm_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    var rmDetails: RmDetails? = null,

    @Column(name = "login_id", length = 100)
    var loginId: String? = null,

    @Column(name = "reject_reason")
    @Enumerated(EnumType.STRING)
    var rejectReason: RejectReason? = null

) : AuditableEntity()
