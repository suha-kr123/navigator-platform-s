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
data class Advisor(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "person_id")
    var personId: UUID? = null,

    @Column(name = "advisor_code", length = 50)
    var advisorCode: String? = null,

    @Column(name = "verification_status", length = 50)
    var verificationStatus: String? = null,

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    var verificationNotes: String? = null,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null
) : AuditableEntity()
