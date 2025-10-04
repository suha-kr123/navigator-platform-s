package com.nivasafinance.features.stagedefinitions.entity

// import com.nivasafinance.features.stagedefinitions.enum.AssignmentStrategy
import com.nivasafinance.common.audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "stage_definitions")
data class StageDefinition(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "key", unique = true, nullable = false)
    val key: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "pipeline_key", nullable = false)
    val pipelineKey: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "possible_outcomes", columnDefinition = "jsonb")
    val possibleOutcomes: List<String>? = null

) : AuditableEntity()
