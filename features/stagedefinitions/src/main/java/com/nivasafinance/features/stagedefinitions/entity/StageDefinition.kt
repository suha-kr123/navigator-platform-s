package com.nivasafinance.features.stagedefinitions.entity

// import com.nivasafinance.features.stagedefinitions.enum.AssignmentStrategy
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

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
    @Column(name = "outcomes", columnDefinition = "jsonb")
    val outcomes: String? = null
)
