package com.nivasafinance.features.leadpipelinemapping.entity

import jakarta.persistence.*
import java.util.*
import audit.AuditableEntity
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "lead_pipeline_mapping")
data class LeadPipelineMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "lead_id", nullable = false)
    val leadId: UUID,

    @Column(name = "pipeline_key", nullable = false)
    val pipelineKey: String,

    @Column(name = "current_stage", nullable = false)
    val currentStage: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    val extData: Map<String, Any>? = null
    
) : AuditableEntity()
