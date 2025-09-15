package com.nivasafinance.features.lead.service

import com.nivasafinance.features.lead.dto.CreateTaskRequest
import com.nivasafinance.features.lead.dto.CreateTaskResponse
import com.nivasafinance.features.lead.repository.LeadRepository
import com.nivasafinance.features.tasks.service.TaskStageMappingService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
@Transactional
class LeadTaskServiceImpl(
    private val leadRepository: LeadRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val taskStageMappingService: TaskStageMappingService
) : LeadTaskService {

    override fun createTaskForLead(leadId: UUID, request: CreateTaskRequest): CreateTaskResponse {
        val lead = leadRepository.findById(leadId)
            .orElseThrow { IllegalArgumentException("Lead not found with id: $leadId") }

        val taskId = UUID.randomUUID()
        val createdAt = LocalDateTime.now()

        val taskData = mapOf(
            "id" to taskId.toString(),
            "taskKey" to request.taskKey,
            "entityId" to leadId.toString(),
            "entityType" to "LEAD",
            "taskData" to (request.taskData ?: ""),
            "assignedTo" to (request.assignedTo ?: ""),
            "status" to "COMPLETED",
            "outcome" to "PENDING",
            "createdAt" to createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "dueAt" to (request.dueAt ?: "")
        )

        val taskKey = "task:$taskId"
        redisTemplate.opsForHash<String, String>().putAll(taskKey, taskData)

        taskStageMappingService.createTaskStageMapping(taskId, leadId, "LEAD", lead.currentStageKey)

        return CreateTaskResponse(
            id = taskId,
            taskKey = request.taskKey,
            entityId = leadId,
            entityType = "LEAD",
            assignedTo = request.assignedTo,
            status = "COMPLETED",
            outcome = "PENDING",
            createdAt = createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }

    override fun getTasksForLead(leadId: UUID): List<CreateTaskResponse> {
        val tasks = mutableListOf<CreateTaskResponse>()
        val taskIds = taskStageMappingService.getTasksForEntity(leadId, "LEAD")

        try {
            taskIds.forEach { taskId ->
                val taskKey = "task:$taskId"
                val taskData = redisTemplate.opsForHash<String, String>().entries(taskKey)
                if (taskData.isNotEmpty()) {
                    tasks.add(
                        CreateTaskResponse(
                            id = taskId,
                            taskKey = taskData["taskKey"] ?: "",
                            entityId = UUID.fromString(taskData["entityId"] ?: ""),
                            entityType = taskData["entityType"] ?: "",
                            assignedTo = taskData["assignedTo"]?.takeIf { it.isNotEmpty() },
                            status = taskData["status"] ?: "",
                            outcome = taskData["outcome"] ?: "",
                            createdAt = taskData["createdAt"] ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handle Redis connection issues gracefully
        }

        return tasks
    }

    override fun getTasksForLeadAndStage(leadId: UUID, stageKey: String): List<CreateTaskResponse> {
        val tasks = mutableListOf<CreateTaskResponse>()
        val taskIds = taskStageMappingService.getTasksForEntityAndStage(leadId, "LEAD", stageKey)

        try {
            taskIds.forEach { taskId ->
                val taskKey = "task:$taskId"
                val taskData = redisTemplate.opsForHash<String, String>().entries(taskKey)
                if (taskData.isNotEmpty()) {
                    tasks.add(
                        CreateTaskResponse(
                            id = taskId,
                            taskKey = taskData["taskKey"] ?: "",
                            entityId = UUID.fromString(taskData["entityId"] ?: ""),
                            entityType = taskData["entityType"] ?: "",
                            assignedTo = taskData["assignedTo"]?.takeIf { it.isNotEmpty() },
                            status = taskData["status"] ?: "",
                            outcome = taskData["outcome"] ?: "",
                            createdAt = taskData["createdAt"] ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handle Redis connection issues gracefully
        }

        return tasks
    }
}
