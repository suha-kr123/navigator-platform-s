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
@Table(name = "stages")
@JaversSpringDataAuditable
@Suppress("ImportOrdering")
data class Stage(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "key", nullable = false, unique = true)
    val key: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "pipeline_key", nullable = false)
    val pipelineKey: String,

    @Column(name = "action_groups", columnDefinition = "jsonb")
    val actionGroups: String? = null,

    @Column(name = "default_tasks", columnDefinition = "jsonb")
    val defaultTasks: String? = null,

    @Column(name = "tasks_allowed", columnDefinition = "jsonb")
    val tasksAllowed: String? = null
)
