package com.nivasafinance.features.tasks.repository

import com.nivasafinance.features.tasks.entity.Task
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TaskRepository : JpaRepository<Task, UUID> {
    fun findAllByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<Task>

    fun existsByEntityTypeAndEntityIdAndTaskDefinitionKey(entityType: String, entityId: UUID, taskDefinitionKey: String): Boolean

    fun findAllByEntityTypeAndEntityIdIn(entityType: String, entityIds: List<UUID>, pageable: Pageable): Page<Task>

    fun findAllByEntityType(entityType: String, pageable: Pageable): Page<Task>
}
