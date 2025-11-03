package com.nivasafinance.features.task.service.impl;

import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.exception.TaskOperationException;
import com.nivasafinance.features.task.exception.TaskValidationException;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.task.repository.TaskRepositoryWrapper;
import com.nivasafinance.features.task.service.TaskWriteService;
import com.nivasafinance.common.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskWriteServiceImpl implements TaskWriteService {

    private final TaskRepositoryWrapper taskRepositoryWrapper;
    private final TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;
    private final MessageSource messageSource;

    @Override
    public TaskResponse createTask(CreateTaskRequest request) {
        validateCreateTaskRequest(request);
        TaskConfig taskConfig = getActiveTaskConfig(request.getTaskConfigKey());
        Task task = buildTaskFromRequest(request);
        Task savedTask = taskRepositoryWrapper.saveWithException(task);
        return TaskResponse.from(savedTask, taskConfig);
    }

    @Override
    public TaskResponse reassignTask(ReassignTaskRequest request) {
        Task task = validateReassignTaskRequest(request);
        updateTaskAssignment(task, request.getNewAssignedTo(), request.getNewAssignedToRole());
        Task savedTask = taskRepositoryWrapper.saveWithException(task);
        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        return TaskResponse.from(savedTask, taskConfig);
    }

    @Override
    public TaskResponse rescheduleTask(RescheduleTaskRequest request) {
        Task oldTask = validateRescheduleTaskRequest(request);
        TaskConfig taskConfig = getActiveTaskConfig(oldTask.getTaskConfigKey());
        
        taskConfigRepositoryWrapper.validateRescheduleAllowed(taskConfig);
        
        closeTaskWithRescheduledOutcome(oldTask, request);
        taskRepositoryWrapper.saveWithException(oldTask);
        
        Task newTask = createRescheduledTask(oldTask, request.getNewDueAt(), request.getReason());
        Task savedNewTask = taskRepositoryWrapper.saveWithException(newTask);
        
        return TaskResponse.from(savedNewTask, taskConfig);
    }

    @Override
    public TaskResponse completeTask(CompleteTaskRequest request) {
        TaskValidationResult validationResult = validateCompleteTaskRequest(request);
        updateTaskOutcome(validationResult.task, request.getOutcome(), request.getOutcomeDetails());
        Task savedTask = taskRepositoryWrapper.saveWithException(validationResult.task);
        return TaskResponse.from(savedTask, validationResult.taskConfig);
    }

    private void validateCreateTaskRequest(CreateTaskRequest request) {
        validateRequestNotNull(request);
        validateIfUserCanCreateTask(request.getTaskConfigKey());
        validateIfTaskConfigExistsAndIsActive(request.getTaskConfigKey());
        validateDueDate(request.getDueAt(), request.getTaskConfigKey());
        validateAssignment(null, request.getAssignedTo(), request.getAssignedToRole());
    }

    private Task validateReassignTaskRequest(ReassignTaskRequest request) {
        validateRequestNotNull(request);
        Task task = taskRepositoryWrapper.findByTaskIdentifierWithException(request.getTaskIdentifier());
        validateTaskNotCompleted(task);
        validateAssignment(task, request.getNewAssignedTo(), request.getNewAssignedToRole());
        return task;
    }

    private Task validateRescheduleTaskRequest(RescheduleTaskRequest request) {
        validateRequestNotNull(request);
        Task task = taskRepositoryWrapper.findByTaskIdentifierWithException(request.getTaskIdentifier());
        validateTaskNotCompleted(task);
        validateDueDate(request.getNewDueAt(), task.getTaskConfigKey());
        return task;
    }

    private TaskValidationResult validateCompleteTaskRequest(CompleteTaskRequest request) {
        validateRequestNotNull(request);
        Task task = taskRepositoryWrapper.findByTaskIdentifierWithException(request.getTaskIdentifier());
        validateTaskNotCompleted(task);
        validateIfUserCanCompleteTask(task.getTaskConfigKey());
        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        taskConfigRepositoryWrapper.validateOutcome(taskConfig, request.getOutcome());
        return new TaskValidationResult(task, taskConfig);
    }

    private void validateRequestNotNull(Object request) {
        if (!ValidationUtils.isNonNull(request)) {
            throw TaskValidationException.requestRequired(messageSource);
        }
    }

    private void validateTaskNotCompleted(Task task) {
        if (ValidationUtils.isNonNull(task.getOutcome())) {
            throw TaskOperationException.alreadyCompleted(task.getTaskIdentifier(), messageSource);
        }
    }

    private void validateAssignment(Task task, String assignedTo, String assignedToRole) {
        if (!ValidationUtils.hasAtLeastOne(assignedTo, assignedToRole)) {
            throw TaskValidationException.assignmentRequired(messageSource);
        }

        if (ValidationUtils.isNonNull(task)) {
            if ((ValidationUtils.isNonNull(assignedTo) && assignedTo.equals(task.getAssignedTo())) ||
                (ValidationUtils.isNonNull(assignedToRole) && assignedToRole.equals(task.getAssignedToRole()))) {
                throw TaskOperationException.cannotReassignToSameUserOrRole(task.getTaskIdentifier(), messageSource);
            }
        }

        //todo: to be implemented by Disa S K after role management feature is implemented
    }
    
    private void validateIfUserCanCreateTask(String taskConfigKey) {
        //todo: to be implemented by Disa S K after role management feature is implemented
    }

    private void validateIfUserCanCompleteTask(String taskConfigKey) {
        //todo: to be implemented by Disa S K after role management feature is implemented
    }

    private void validateDueDate(LocalDateTime dueAt, String taskConfigKey) {
        if (ValidationUtils.isNonNull(dueAt) && dueAt.isBefore(LocalDateTime.now())) {
            throw TaskOperationException.dueDateInPast(taskConfigKey, messageSource);
        }
    }

    private void validateIfTaskConfigExistsAndIsActive(String taskConfigKey) {
        taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey);
    }

    private Task buildTaskFromRequest(CreateTaskRequest request) {
        Task task = new Task();
        task.setTaskConfigKey(request.getTaskConfigKey());
        task.setAssignedTo(request.getAssignedTo());
        task.setAssignedToRole(request.getAssignedToRole());
        task.setDueAt(request.getDueAt());
        task.setTaskDetails(ValidationUtils.isNonNull(request.getTaskDetails()) ? request.getTaskDetails() : new HashMap<>());
        return task;
    }

    private TaskConfig getActiveTaskConfig(String taskConfigKey) {
        return taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey);
    }

    private void updateTaskAssignment(Task task, String assignedTo, String assignedToRole) {
        task.setAssignedTo(assignedTo);
        task.setAssignedToRole(assignedToRole);
    }

    private void closeTaskWithRescheduledOutcome(Task task, RescheduleTaskRequest request) {
        Map<String, Object> outcomeDetails = new HashMap<>();
        outcomeDetails.put("rescheduledToNewDueDate", request.getNewDueAt());
        if (ValidationUtils.isNonNullOrEmpty(request.getReason())) {
            outcomeDetails.put("rescheduleReason", request.getReason());
        }
        updateTaskOutcome(task, "RESCHEDULED", outcomeDetails);
    }

    private Task createRescheduledTask(Task oldTask, LocalDateTime newDueAt, String reason) {
        Task newTask = new Task();
        newTask.setTaskConfigKey(oldTask.getTaskConfigKey());
        newTask.setAssignedTo(oldTask.getAssignedTo());
        newTask.setAssignedToRole(oldTask.getAssignedToRole());
        newTask.setDueAt(newDueAt);
        
        Map<String, Object> taskDetails = ValidationUtils.isNonNull(oldTask.getTaskDetails()) 
            ? new HashMap<>(oldTask.getTaskDetails()) 
            : new HashMap<>();
        taskDetails.put("rescheduledFromTaskId", oldTask.getId());
        if (ValidationUtils.isNonNullOrEmpty(reason)) {
            taskDetails.put("rescheduleReason", reason);
        }
        newTask.setTaskDetails(taskDetails);
        
        return newTask;
    }

    private void updateTaskOutcome(Task task, String outcome, Map<String, Object> outcomeDetails) {
        task.setOutcome(outcome);
        task.setOutcomeDetails(ValidationUtils.isNonNull(outcomeDetails) ? outcomeDetails : new HashMap<>());
    }

    private static class TaskValidationResult {
        private final Task task;
        private final TaskConfig taskConfig;

        public TaskValidationResult(Task task, TaskConfig taskConfig) {
            this.task = task;
            this.taskConfig = taskConfig;
        }
    }
}

