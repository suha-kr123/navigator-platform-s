package com.nivasafinance.features.advisor.entity

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
import org.javers.core.metamodel.annotation.TypeName
import java.util.UUID

@Entity
@TypeName("advisor")
@Table(name = "advisor")
@NoArg
@Suppress("LongParameterList")
class Advisor(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "person_id")
    val personId: UUID,

    @Column(name = "advisor_code")
    val advisorCode: String?,

    @Column(name = "is_employee")
    val isEmployee: Boolean = false,

    @Column(name = "status")
    val status: String,

    @Column(name = "is_experienced_dsa")
    val isExperiencedDsa: Boolean = false,

    @Column(name = "remarks", length = 1000)
    val remarks: String? = null,

    @Column(name = "rejection_reason", length = 1000)
    val rejectionReason: String? = null,

    @Column(name = "advisor_feedback", length = 2000)
    val advisorFeedback: String? = null,

    @Column(name = "welcome_kit_sent")
    val welcomeKitSent: Boolean = false,

    @Column(name = "attended_advisor_meeting")
    val attendedAdvisorMeeting: Boolean = false,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    val extData: Map<String, Any>? = null
) : AuditableEntity()
