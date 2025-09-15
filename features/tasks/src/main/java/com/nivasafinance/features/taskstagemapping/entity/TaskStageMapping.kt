package com.nivasafinance.features.taskstagemapping.entity

import jakarta.persistence.*
import java.util.*
import audit.AuditableEntity

@Entity
@Table(name = "task_stage_mappings")
data class TaskStageMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "task_id", nullable = false)
    val taskId: UUID,

    @Column(name = "stage_key", nullable = false)
    val stageKey: String,
    
) : AuditableEntity()
