package com.nivasafinance.features.stagedefinitions.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "transition_definitions")
data class TransitionDefinition(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "pipeline", nullable = false)
    val pipeline: String,

    @Column(name = "from_stage", nullable = false)
    val fromStage: String,

    @Column(name = "to_stage", nullable = false)
    val toStage: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition_on_transition", columnDefinition = "jsonb")
    val conditionOnTransition: String? = null
)
