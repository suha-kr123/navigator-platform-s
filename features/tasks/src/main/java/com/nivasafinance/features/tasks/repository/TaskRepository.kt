package com.nivasafinance.features.tasks.repository

import com.nivasafinance.features.tasks.entity.Task
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TaskRepository : JpaRepository<Task, UUID> {
    @Query("select case when count(t) > 0 then true else false end from Task t where t.taskDefinitionKey = :taskKey")
    fun existsByTaskKey(@Param("taskKey") taskKey: String): Boolean
    
    fun findAllByAssignedTo(assignedTo: String, pageable: Pageable): Page<Task>
}
