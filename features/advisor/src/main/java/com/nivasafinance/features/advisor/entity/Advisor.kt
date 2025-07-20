package com.nivasafinance.features.advisor.entity

import annotations.NoArg
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.person.entity.Details
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "advisor")
@NoArg

@Suppress("LongParameterList")
class Advisor(
    @Id
    val id: UUID,

    @Column(name = "person_id")
    val personId: UUID,

    @Column(name = "advisor_code")
    val advisorCode: String,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    val status: AdvisorStatus,

    @Type(JsonType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    val dataExt: Details? = null,
)

@NoArg
data class Details(
    val description: String,
)
