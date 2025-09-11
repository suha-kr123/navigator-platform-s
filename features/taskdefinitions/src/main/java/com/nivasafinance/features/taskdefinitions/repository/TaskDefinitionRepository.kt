package com.nivasafinance.features.taskdefinitions.repository

import com.nivasafinance.features.taskdefinitions.entity.TaskDefinition
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface TaskDefinitionRepository : JpaRepository<TaskDefinition, UUID> {

    fun findByKey(key: String): TaskDefinition?

    fun findByIdentifier(identifier: String): List<TaskDefinition>

    fun findByActionsGroup(actionsGroup: String): List<TaskDefinition>

    fun existsByKey(key: String): Boolean

    @Query("SELECT td FROM TaskDefinition td WHERE td.actionsGroup = :actionsGroup AND td.priority = :priority")
    fun findByActionsGroupAndPriority(
        @Param("actionsGroup") actionsGroup: String,
        @Param("priority") priority: String
    ): List<TaskDefinition>

    @Query("SELECT td FROM TaskDefinition td WHERE td.assignmentStrategy = :strategy")
    fun findByAssignmentStrategy(@Param("strategy") strategy: String): List<TaskDefinition>
}
