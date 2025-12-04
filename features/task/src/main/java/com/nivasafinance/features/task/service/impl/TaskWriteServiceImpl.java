package com.nivasafinance.features.task.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.task.dto.BulkReassignTaskRequest;
import com.nivasafinance.features.task.dto.BulkReassignTaskResponse;
import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.dto.UpdateDueDateRequest;
import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.exception.TaskOperationException;
import com.nivasafinance.features.task.exception.TaskValidationException;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.task.repository.TaskRepositoryWrapper;
import com.nivasafinance.features.task.service.DueDateCalculatorService;
import com.nivasafinance.features.task.service.TaskConfigValidationService;
import com.nivasafinance.features.task.service.TaskWriteService;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.common.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskWriteServiceImpl implements TaskWriteService {

    private final TaskRepositoryWrapper taskRepositoryWrapper;
    private final TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;
    private final TaskConfigValidationService taskConfigValidationService;
    private final DueDateCalculatorService dueDateCalculatorService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    @Override
    public TaskResponse createTask(CreateTaskRequest request) {
        validateCreateTaskRequest(request);
        TaskConfig taskConfig = getActiveTaskConfig(request.getTaskConfigKey());
        
        // Calculate due date from task config if not provided
        LocalDateTime dueAt = request.getDueAt();
        if (!ValidationUtils.isNonNull(dueAt)) {
            dueAt = calculateDueDateFromConfig(taskConfig, request);
        }
        
        // Create a new request with calculated due date
        CreateTaskRequest requestWithDueDate = CreateTaskRequest.builder()
                .taskConfigKey(request.getTaskConfigKey())
                .assignedTo(request.getAssignedTo())
                .dueAt(dueAt)
                .taskDetails(request.getTaskDetails())
                .build();
        
        Task task = buildTaskFromRequest(requestWithDueDate);
        Task savedTask = taskRepositoryWrapper.saveWithException(task);
        return TaskResponse.from(savedTask, taskConfig, objectMapper);
    }

    @Override
    public TaskResponse reassignTask(ReassignTaskRequest request) {
        Task task = validateReassignTaskRequest(request);
        updateTaskAssignment(task, request.getNewAssignedTo());
        Task savedTask = taskRepositoryWrapper.saveWithException(task);
        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        return TaskResponse.from(savedTask, taskConfig, objectMapper);
    }

    @Override
    public TaskResponse updateDueDate(UpdateDueDateRequest request) {
        Task task = validateUpdateDueDateRequest(request);
        task.setDueAt(request.getDueAt());
        Task savedTask = taskRepositoryWrapper.saveWithException(task);
        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        return TaskResponse.from(savedTask, taskConfig, objectMapper);
    }

    @Override
    public TaskResponse rescheduleTask(RescheduleTaskRequest request) {
        Task oldTask = validateRescheduleTaskRequest(request);
        TaskConfig taskConfig = getActiveTaskConfig(oldTask.getTaskConfigKey());
        
        taskConfigValidationService.validateRescheduleAllowed(taskConfig);
        
        closeTaskWithRescheduledOutcome(oldTask, request);
        taskRepositoryWrapper.saveWithException(oldTask);
        
        return TaskResponse.from(oldTask, taskConfig, objectMapper);
    }

    @Override
    public TaskResponse completeTask(CompleteTaskRequest request) {
        TaskValidationResult validationResult = validateCompleteTaskRequest(request);
    
        
        Task.OutcomeDetails outcomeDetails = Task.OutcomeDetails.builder()
                .remarks(ValidationUtils.isNonNull(request.getOutcomeDetails()) 
                        ? request.getOutcomeDetails().getRemarks() 
                        : null)
                .completedAt(LocalDateTime.now())
                .completedBy(UserContext.getUsername())
                .build();
        
        updateTaskOutcome(validationResult.task, request.getOutcomeCodeValueKey(), outcomeDetails);
        Task savedTask = taskRepositoryWrapper.saveWithException(validationResult.task);
        return TaskResponse.from(savedTask, validationResult.taskConfig, objectMapper);
    }

    private void validateCreateTaskRequest(CreateTaskRequest request) {
        validateRequestNotNull(request);
        validateIfTaskConfigExistsAndIsActive(request.getTaskConfigKey());
        validateDueDate(request.getDueAt(), request.getTaskConfigKey());
    }

    private Task validateReassignTaskRequest(ReassignTaskRequest request) {
        validateRequestNotNull(request);
        Task task = taskRepositoryWrapper.findByTaskIdentifierWithException(request.getTaskIdentifier());
        validateTaskNotCompleted(task);
        validateAssignment(task, request.getNewAssignedTo());
        return task;
    }

    private Task validateRescheduleTaskRequest(RescheduleTaskRequest request) {
        validateRequestNotNull(request);
        Task task = taskRepositoryWrapper.findByTaskIdentifierWithException(request.getTaskIdentifier());
        validateTaskNotCompleted(task);
        validateDueDate(request.getPreferredEndTime(), task.getTaskConfigKey());
        return task;
    }

    private Task validateUpdateDueDateRequest(UpdateDueDateRequest request) {
        validateRequestNotNull(request);
        Task task = taskRepositoryWrapper.findByTaskIdentifierWithException(request.getTaskIdentifier());
        validateTaskNotCompleted(task);
        validateDueDate(request.getDueAt(), task.getTaskConfigKey());
        return task;
    }

    private TaskValidationResult validateCompleteTaskRequest(CompleteTaskRequest request) {
        validateRequestNotNull(request);
        Task task = taskRepositoryWrapper.findByTaskIdentifierWithException(request.getTaskIdentifier());
        validateTaskNotCompleted(task);
        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        taskConfigValidationService.validateOutcome(taskConfig, request.getOutcomeCodeValueKey());
        return new TaskValidationResult(task, taskConfig);
    }

    private void validateRequestNotNull(Object request) {
        ValidationUtils.requireNonNull(request, () -> TaskValidationException.requestRequired(messageSource));
    }

    private void validateTaskNotCompleted(Task task) {
        if (ValidationUtils.isNonNull(task.getOutcome())) {
            throw TaskOperationException.alreadyCompleted(task.getTaskIdentifier(), messageSource);
        }
    }

    private void validateAssignment(Task task, String assignedTo) {
        if (!ValidationUtils.isNonNull(assignedTo)) {
            throw TaskValidationException.assignmentRequired(messageSource);
        }

        if (ValidationUtils.isNonNull(task)) {
            if (assignedTo.equals(task.getAssignedTo())) {
                throw TaskOperationException.cannotReassignToSameUserOrRole(task.getTaskIdentifier(), messageSource);
            }
        }
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
        task.setDueAt(request.getDueAt());
        
        Task.TaskDetails taskDetails = null;
        if (ValidationUtils.isNonNull(request.getTaskDetails())) {
            Task.TaskDetails.PreferredCallWindow preferredCallWindow = null;
            if (ValidationUtils.isNonNull(request.getTaskDetails().getPreferredCallWindow())) {
                LocalDateTime start = request.getTaskDetails().getPreferredCallWindow().getStart();
                LocalDateTime end = request.getTaskDetails().getPreferredCallWindow().getEnd();
                // Only create preferredCallWindow if both start and end are provided
                if (ValidationUtils.isNonNull(start) && ValidationUtils.isNonNull(end)) {
                    preferredCallWindow = Task.TaskDetails.PreferredCallWindow.builder()
                            .start(start)
                            .end(end)
                            .build();
                }
            }
            taskDetails = Task.TaskDetails.builder()
                    .entityId(request.getTaskDetails().getEntityId())
                    .entityType(request.getTaskDetails().getEntityType())
                    .preferredCallWindow(preferredCallWindow)
                    .creatorRemarks(request.getTaskDetails().getCreatorRemarks())
                    .iterationCount(ValidationUtils.isNonNull(request.getTaskDetails().getIterationCount()) 
                            ? request.getTaskDetails().getIterationCount() 
                            : 0)
                    .rescheduledFromTaskIdentifier(request.getTaskDetails().getRescheduledFromTaskIdentifier())
                    .build();
        }
        task.setTaskDetails(taskDetails);
        return task;
    }

    private TaskConfig getActiveTaskConfig(String taskConfigKey) {
        return taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey);
    }

    /**
     * Calculates due date from task config's dueDateLogicExpression if available.
     * Returns null if expression is not configured or evaluation fails.
     */
    private LocalDateTime calculateDueDateFromConfig(TaskConfig taskConfig, CreateTaskRequest request) {
        if (taskConfig == null || taskConfig.getTaskConfigDetails() == null) {
            return null;
        }
        
        String expression = taskConfig.getTaskConfigDetails().getDueDateLogicExpression();
        if (!ValidationUtils.isNonNullOrEmpty(expression)) {
            return null;
        }
        
        // Build context for SpEL evaluation
        Map<String, Object> context = new HashMap<>();
        
        // Add task details if available
        if (ValidationUtils.isNonNull(request.getTaskDetails())) {
            if (ValidationUtils.isNonNull(request.getTaskDetails().getEntityId())) {
                context.put("entityId", request.getTaskDetails().getEntityId());
            }
            if (ValidationUtils.isNonNull(request.getTaskDetails().getEntityType())) {
                context.put("entityType", request.getTaskDetails().getEntityType());
            }
        }
        
        // Add assignedTo if available
        if (ValidationUtils.isNonNull(request.getAssignedTo())) {
            context.put("assignedTo", request.getAssignedTo());
        }
        
        return dueDateCalculatorService.calculateDueDate(expression, context);
    }

    private void updateTaskAssignment(Task task, String assignedTo) {
        task.setAssignedTo(assignedTo);
    }

    private void closeTaskWithRescheduledOutcome(Task task, RescheduleTaskRequest request) {
        Task.OutcomeDetails outcomeDetails = Task.OutcomeDetails.builder()
                .rescheduleReasonCodeValueKey(request.getReasonCodeValueKey())
                .build();
        updateTaskOutcome(task, "RESCHEDULED", outcomeDetails);
    }


    private void updateTaskOutcome(Task task, String outcome, Task.OutcomeDetails outcomeDetails) {
        task.setOutcome(outcome);
        task.setOutcomeDetails(outcomeDetails);
    }

    @Override
    public BulkReassignTaskResponse bulkReassignTasks(BulkReassignTaskRequest request) {
        List<UUID> successfulTaskIdentifiers = new ArrayList<>();
        List<BulkReassignTaskResponse.BulkAssignmentError> errors = new ArrayList<>();
        
        for (UUID taskIdentifier : request.getTaskIdentifiers()) {
            try {
                ReassignTaskRequest reassignRequest = ReassignTaskRequest.builder()
                        .taskIdentifier(taskIdentifier)
                        .newAssignedTo(request.getNewAssignedTo())
                        .build();
                
                reassignTask(reassignRequest);
                successfulTaskIdentifiers.add(taskIdentifier);
            } catch (Exception e) {
                errors.add(BulkReassignTaskResponse.BulkAssignmentError.builder()
                        .taskIdentifier(taskIdentifier)
                        .errorMessage(e.getMessage())
                        .build());
            }
        }
        
        return BulkReassignTaskResponse.builder()
                .totalRequested(request.getTaskIdentifiers().size())
                .successful(successfulTaskIdentifiers.size())
                .failed(errors.size())
                .successfulTaskIdentifiers(successfulTaskIdentifiers)
                .errors(errors)
                .build();
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

