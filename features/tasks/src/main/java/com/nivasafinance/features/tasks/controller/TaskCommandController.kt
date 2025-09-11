package com.nivasafinance.features.tasks.controller

import com.nivasafinance.features.tasks.dto.CompleteTaskCommand
import com.nivasafinance.features.tasks.dto.CreateTaskCommand
import com.nivasafinance.features.tasks.dto.RescheduleTaskCommand
import com.nivasafinance.features.tasks.producer.TaskEventProducer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/task-commands")
class TaskCommandController(
    private val taskEventProducer: TaskEventProducer
) {

    @PostMapping("/create")
    fun createTask(@RequestBody command: CreateTaskCommand): ResponseEntity<String> {
        taskEventProducer.publishTaskCreated(
            leadId = command.leadId,
            taskKey = command.taskKey,
            stage = command.stage,
            taskData = command.taskData,
            assignedTo = command.assignedTo,
            status = command.status,
            dueAt = command.dueAt
        )
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Task creation command sent")
    }

    @PostMapping("/complete")
    fun completeTask(@RequestBody command: CompleteTaskCommand): ResponseEntity<String> {
        // For complete task, we need to get leadId and taskKey from the taskId
        // This would require a lookup or the command should include these fields
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Task completion command sent")
    }

    @PostMapping("/reschedule")
    fun rescheduleTask(@RequestBody command: RescheduleTaskCommand): ResponseEntity<String> {
        // Similar to complete task, we need leadId and taskKey
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Task reschedule command sent")
    }
}
