package com.nivasafinance.features.taskdefinitions.repository

import com.nivasafinance.features.taskdefinitions.entity.TaskDefinition
import com.nivasafinance.features.taskdefinitions.enum.AssignmentStrategy
import com.nivasafinance.features.taskdefinitions.enum.Priority
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TaskDefinitionRepository : JpaRepository<TaskDefinition, UUID> {
}
