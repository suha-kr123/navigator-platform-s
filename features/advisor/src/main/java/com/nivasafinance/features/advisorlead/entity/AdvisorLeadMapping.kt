package com.nivasafinance.features.advisorlead.entity

import com.nivasafinance.common.annotations.NoArg
import com.nivasafinance.common.audit.AuditableEntity
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
@Table(name = "n_advisor_lead_mapping")
@NoArg
@Suppress("LongParameterList")
data class AdvisorLeadMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "advisor_id")
    var advisorId: Long? = null,

    @Column(name = "lead_id")
    var leadId: Long? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null
) : AuditableEntity()
