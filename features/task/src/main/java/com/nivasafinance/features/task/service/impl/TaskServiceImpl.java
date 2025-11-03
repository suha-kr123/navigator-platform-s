package com.nivasafinance.features.task.service.impl;

import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.exception.TaskConfigNotFoundException;
import com.nivasafinance.features.task.exception.TaskNotFoundException;
import com.nivasafinance.features.task.exception.TaskOperationException;
import com.nivasafinance.features.task.exception.TaskValidationException;
import com.nivasafinance.features.task.repository.TaskConfigRepository;
import com.nivasafinance.features.task.repository.TaskRepository;
import com.nivasafinance.features.task.service.TaskService;
import com.nivasafinance.common.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskConfigRepository taskConfigRepository;
    private final MessageSource messageSource;

    @Override
    public TaskResponse createTask(CreateTaskRequest request) {
        log.info("Creating task for config: {}", request.getTaskConfigKey());

        // Validate request
        validateCreateTaskRequest(request);

        // Validate task config exists and is active
        TaskConfig taskConfig = getActiveTaskConfig(request.getTaskConfigKey());

        // Create task entity
        Task task = new Task();
        task.setTaskConfigKey(request.getTaskConfigKey());
        task.setAssignedTo(request.getAssignedTo());
        task.setAssignedToRole(request.getAssignedToRole());
        task.setDueAt(request.getDueAt());
        task.setTaskDetails(ValidationUtils.isNonNull(request.getTaskDetails()) ? request.getTaskDetails() : new HashMap<>());

        // Save task
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());

        return TaskResponse.from(savedTask, taskConfig);
    }

    @Override
    public TaskResponse reassignTask(ReassignTaskRequest request) {
        log.info("Reassigning task ID: {} to user: {} or role: {}", 
                request.getTaskId(), request.getNewAssignedTo(), request.getNewAssignedToRole());

        // Validate request
        validateReassignTaskRequest(request);

        // Get task
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new TaskNotFoundException(request.getTaskId(), messageSource));

        // Validate task is not completed
        if (ValidationUtils.isNonNull(task.getOutcome())) {
            throw TaskOperationException.cannotReassignCompleted(request.getTaskId(), messageSource);
        }

        // Update assignment
        task.setAssignedTo(request.getNewAssignedTo());
        task.setAssignedToRole(request.getNewAssignedToRole());

        // Save task - updatedBy from AuditableEntity will capture who reassigned it
        Task savedTask = taskRepository.save(task);
        log.info("Task reassigned successfully");

        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        return TaskResponse.from(savedTask, taskConfig);
    }

    @Override
    public TaskResponse rescheduleTask(RescheduleTaskRequest request) {
        log.info("Rescheduling task ID: {} to {}", request.getTaskId(), request.getNewDueAt());

        // Validate request
        validateRescheduleTaskRequest(request);

        // Get task
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new TaskNotFoundException(request.getTaskId(), messageSource));

        // Validate task is not completed
        if (ValidationUtils.isNonNull(task.getOutcome())) {
            throw TaskOperationException.cannotRescheduleCompleted(request.getTaskId(), messageSource);
        }

        // Update due date
        task.setDueAt(request.getNewDueAt());

        // Add reschedule reason to task details if provided
        if (ValidationUtils.isNonNullOrEmpty(request.getReason())) {
            Map<String, Object> taskDetails = ValidationUtils.isNonNull(task.getTaskDetails()) ? task.getTaskDetails() : new HashMap<>();
            taskDetails.put("rescheduleReason", request.getReason());
            task.setTaskDetails(taskDetails);
        }

        // Save task - updatedBy from AuditableEntity will capture who rescheduled it
        Task savedTask = taskRepository.save(task);
        log.info("Task rescheduled successfully to {}", request.getNewDueAt());

        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        return TaskResponse.from(savedTask, taskConfig);
    }

    @Override
    public TaskResponse completeTask(CompleteTaskRequest request) {
        log.info("Completing task ID: {}", request.getTaskId());

        // Validate request
        validateCompleteTaskRequest(request);

        // Get task
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new TaskNotFoundException(request.getTaskId(), messageSource));

        // Validate task can be completed
        if (ValidationUtils.isNonNull(task.getOutcome())) {
            throw TaskOperationException.alreadyCompleted(request.getTaskId(), messageSource);
        }

        // Validate outcome is valid for this task config
        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        validateOutcome(taskConfig, request.getOutcome());

        // Update task with outcome
        task.setOutcome(request.getOutcome());
        task.setOutcomeDetails(ValidationUtils.isNonNull(request.getOutcomeDetails()) ? request.getOutcomeDetails() : new HashMap<>());

        // Save task - updatedBy from AuditableEntity will capture who completed it
        Task savedTask = taskRepository.save(task);
        log.info("Task completed successfully with outcome: {}", request.getOutcome());

        return TaskResponse.from(savedTask, taskConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        log.debug("Getting task by ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId, messageSource));
        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        return TaskResponse.from(task, taskConfig);
    }

    // Private helper methods

    private TaskConfig getActiveTaskConfig(String taskConfigKey) {
        TaskConfig taskConfig = taskConfigRepository.findByTaskConfigKey(taskConfigKey)
                .orElseThrow(() -> new TaskConfigNotFoundException(taskConfigKey, messageSource));

        if (!Boolean.TRUE.equals(taskConfig.getIsActive())) {
            throw TaskOperationException.taskConfigInactive(taskConfigKey, messageSource);
        }

        return taskConfig;
    }

    private void validateCreateTaskRequest(CreateTaskRequest request) {
        if (!ValidationUtils.isNonNull(request)) {
            throw TaskValidationException.requestRequired(messageSource);
        }
        //validate task config key and as well check if that exists in task config table
        TaskConfig taskConfig = taskConfigRepository.findByTaskConfigKey(request.getTaskConfigKey())
                .orElseThrow(() -> new TaskConfigNotFoundException(request.getTaskConfigKey(), messageSource));
        if (!Boolean.TRUE.equals(taskConfig.getIsActive())) {
            throw TaskOperationException.taskConfigInactive(request.getTaskConfigKey(), messageSource);
        }
        //check if assigned to or assigned to role is provided (at least one)
        if (!ValidationUtils.hasAtLeastOne(request.getAssignedTo(), request.getAssignedToRole())) {
            throw TaskValidationException.assignmentRequired(messageSource);
        }
        //make sure due date can't be in the past (if provided)
        if (ValidationUtils.isNonNull(request.getDueAt()) && request.getDueAt().isBefore(LocalDateTime.now())) {
            throw TaskOperationException.dueDateInPast(request.getTaskConfigKey(), messageSource);
        }
    }

    private void validateReassignTaskRequest(ReassignTaskRequest request) {
        if (!ValidationUtils.isNonNull(request)) {
            throw TaskValidationException.requestRequired(messageSource);
        }
        if (!ValidationUtils.isNonNull(request.getTaskId())) {
            throw TaskValidationException.taskIdRequired(messageSource);
        }
        if (!ValidationUtils.hasAtLeastOne(request.getNewAssignedTo(), request.getNewAssignedToRole())) {
            throw TaskValidationException.newAssignmentRequired(messageSource);
        }
    }

    private void validateRescheduleTaskRequest(RescheduleTaskRequest request) {
        if (!ValidationUtils.isNonNull(request)) {
            throw TaskValidationException.requestRequired(messageSource);
        }
        if (!ValidationUtils.isNonNull(request.getTaskId())) {
            throw TaskValidationException.taskIdRequired(messageSource);
        }
        if (!ValidationUtils.isNonNull(request.getNewDueAt())) {
            throw TaskValidationException.dueDateRequired(messageSource);
        }
    }

    private void validateCompleteTaskRequest(CompleteTaskRequest request) {
        if (!ValidationUtils.isNonNull(request)) {
            throw TaskValidationException.requestRequired(messageSource);
        }
        if (!ValidationUtils.isNonNull(request.getTaskId())) {
            throw TaskValidationException.taskIdRequired(messageSource);
        }
        if (!ValidationUtils.isNonNullOrEmpty(request.getOutcome())) {
            throw TaskValidationException.outcomeRequired(messageSource);
        }
    }

    private void validateOutcome(TaskConfig taskConfig, String outcome) {
        Map<String, Object> configDetails = taskConfig.getTaskConfigDetails();
        if (ValidationUtils.isNonNull(configDetails) && configDetails.containsKey("allowedOutcomes")) {
            Object allowedOutcomes = configDetails.get("allowedOutcomes");
            if (allowedOutcomes instanceof List) {
                if (!((List<?>) allowedOutcomes).contains(outcome)) {
                    throw TaskValidationException.invalidOutcome(outcome, taskConfig.getTaskConfigKey(), messageSource);
                }
            }
        }
    }
}

