package com.nivasafinance.features.tasks.repository

import com.nivasafinance.features.tasks.entity.Task
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TaskRepository : JpaRepository<Task, UUID> {
    fun existsByTaskKey(taskKey: String): Boolean
    
    fun findAllByAssignedTo(assignedTo: String, pageable: Pageable): Page<Task>
}
