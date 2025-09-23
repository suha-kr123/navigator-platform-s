package com.nivasafinance.features.taskdefinitions.entity

import audit.AuditableEntity
// import com.nivasafinance.features.taskdefinitions.enum.AssignmentStrategy
import com.nivasafinance.features.taskdefinitions.enum.TaskType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "task_definitions")
data class TaskDefinition(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "key", unique = true, nullable = false)
    val key: String,

    @Column(name = "type", nullable = false)
    val type: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "possible_outcomes", columnDefinition = "jsonb")
    val possibleOutcomes: List<String>? = null

) : AuditableEntity()
