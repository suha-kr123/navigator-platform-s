package com.nivasafinance.features.taskdefinitions.entity

// import com.nivasafinance.features.taskdefinitions.enum.AssignmentStrategy
import com.nivasafinance.common.audit.AuditableEntity
import com.nivasafinance.features.taskdefinitions.enum.TaskType
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
    val type: TaskType,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "possible_outcomes", columnDefinition = "jsonb")
    val possibleOutcomes: List<String>? = null

) : AuditableEntity()
