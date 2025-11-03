package com.nivasafinance.features.task.service;

import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;

/**
 * Task Write Service - Handles all write operations for tasks
 * 
 * This service handles task mutations including creation, updates, and state changes.
 * Designed to be used internally by the Workflow Engine or other upstream modules.
 */
public interface TaskWriteService {

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
     * Creates a new task and closes the old one with RESCHEDULED outcome
     * 
     * @param request the reschedule request
     * @return the newly created task response
     */
    TaskResponse rescheduleTask(RescheduleTaskRequest request);

    /**
     * Complete a task with outcome
     * 
     * @param request the task completion request
     * @return the completed task response
     */
    TaskResponse completeTask(CompleteTaskRequest request);
}

