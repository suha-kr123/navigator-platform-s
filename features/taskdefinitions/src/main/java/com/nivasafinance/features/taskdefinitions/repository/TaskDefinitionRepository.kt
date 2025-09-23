package com.nivasafinance.features.taskdefinitions.repository

import com.nivasafinance.features.taskdefinitions.entity.TaskDefinition
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TaskDefinitionRepository : JpaRepository<TaskDefinition, UUID> {
    fun findByKey(key: String): TaskDefinition?
}
