package com.nivasafinance.features.task.service;

import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;

/**
 * Task Service - Internal Logic Layer
 * 
 * This service handles all internal task operations including creation, completion,
 * rescheduling, validation, etc. It is designed to be used internally by the Workflow
 * Engine or other upstream modules.
 */
public interface TaskService {

    /**
     * Create a new task
     * 
     * @param request the task creation request
     * @return the created task response
     */
    TaskResponse createTask(CreateTaskRequest request);

    /**
     * Reassign a task to a different user or role
     * 
     * @param request the reassign request
     * @return the reassigned task response
     */
    TaskResponse reassignTask(ReassignTaskRequest request);

    /**
     * Reschedule a task to a new due date
     * 
     * @param request the reschedule request
     * @return the rescheduled task response
     */
    TaskResponse rescheduleTask(RescheduleTaskRequest request);

    /**
     * Complete a task with outcome
     * 
     * @param request the task completion request
     * @return the completed task response
     */
    TaskResponse completeTask(CompleteTaskRequest request);

    /**
     * Get task by ID
     * 
     * @param taskId the task ID
     * @return the task response
     */
    TaskResponse getTaskById(Long taskId);
}

