package com.nivasafinance.features.advisor.entity

import com.nivasafinance.common.annotations.NoArg
import com.nivasafinance.common.audit.AuditableEntity
import com.nivasafinance.features.advisor.enum.AdvisorStatus
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: AdvisorStatus,

    @Column(name = "rejection_reason_key", length = 50)
    val rejectionReasonKey: String? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    val extData: Map<String, Any>? = null
) : AuditableEntity()
