package com.nivasafinance.features.stages.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Table
import org.javers.core.metamodel.annotation.Id
import org.javers.spring.annotation.JaversSpringDataAuditable
import java.util.UUID

@Entity
@Table(name = "transition_definitions")
@JaversSpringDataAuditable
@Suppress("ImportOrdering")
data class TransitionDefinition(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "pipeline", nullable = false)
    val pipeline: String,

    @Column(name = "from_stage", nullable = false)
    val fromStage: String,

    @Column(name = "to_stage", nullable = false)
    val toStage: String,

    @Column(name = "condition_on_transition", columnDefinition = "jsonb")
    val conditionOnTransition: String? = null
)
