package com.nivasafinance.features.stagetasks.entity

import audit.AuditableEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "stage_tasks")
data class StageTask(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "stage_id", nullable = false)
    val stageId: UUID,

    @Column(name = "task_id", nullable = false)
    val taskId: UUID,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    val extData: Map<String, Any>? = null

) : AuditableEntity()
