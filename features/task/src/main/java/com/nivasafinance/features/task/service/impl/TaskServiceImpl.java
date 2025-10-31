package com.nivasafinance.features.task.service.impl;

import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.dto.UpdateTaskRequest;
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
import java.util.stream.Collectors;

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
        task.setTaskDetails(request.getTaskDetails() != null ? request.getTaskDetails() : new HashMap<>());

        // Save task
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());

        return mapToTaskResponse(savedTask, taskConfig);
    }

    @Override
    public TaskResponse updateTask(UpdateTaskRequest request) {
        log.info("Updating task ID: {}", request.getTaskId());

        // Validate request
        validateUpdateTaskRequest(request);

        // Get task
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new TaskNotFoundException(request.getTaskId(), messageSource));

        // Validate task is not completed
        if (task.getOutcome() != null) {
            throw new TaskOperationException(
                    "error.task.cannot.update.completed",
                    new Object[]{request.getTaskId()},
                    messageSource
            );
        }

        // Update fields only if provided (patch operation)
        if (request.getAssignedTo() != null) {
            task.setAssignedTo(request.getAssignedTo());
        }
        if (request.getAssignedToRole() != null) {
            task.setAssignedToRole(request.getAssignedToRole());
        }
        if (request.getDueAt() != null) {
            task.setDueAt(request.getDueAt());
        }
        if (request.getTaskDetails() != null) {
            // Merge task details instead of replacing
            Map<String, Object> existingDetails = task.getTaskDetails() != null ? task.getTaskDetails() : new HashMap<>();
            existingDetails.putAll(request.getTaskDetails());
            task.setTaskDetails(existingDetails);
        }

        // Save task - updatedBy from AuditableEntity will capture who updated it
        Task savedTask = taskRepository.save(task);
        log.info("Task updated successfully");

        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        return mapToTaskResponse(savedTask, taskConfig);
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
        if (task.getOutcome() != null) {
            throw new TaskOperationException(
                    "error.task.already.completed",
                    new Object[]{request.getTaskId()},
                    messageSource
            );
        }

        // Validate outcome is valid for this task config
        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        validateOutcome(taskConfig, request.getOutcome());

        // Update task with outcome
        task.setOutcome(request.getOutcome());
        task.setOutcomeDetails(request.getOutcomeDetails() != null ? request.getOutcomeDetails() : new HashMap<>());

        // Save task - updatedBy from AuditableEntity will capture who completed it
        Task savedTask = taskRepository.save(task);
        log.info("Task completed successfully with outcome: {}", request.getOutcome());

        return mapToTaskResponse(savedTask, taskConfig);
    }

    @Override
    public TaskResponse cancelTask(Long taskId, String reason) {
        log.info("Cancelling task ID: {}", taskId);

        // Validate inputs
        if (!ValidationUtils.isNonNull(taskId)) {
            throw new TaskValidationException("error.task.id.required", null, messageSource);
        }

        // Get task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId, messageSource));

        // Validate task is not already completed or cancelled
        if (task.getOutcome() != null) {
            throw new TaskOperationException(
                    "error.task.cannot.cancel.completed",
                    new Object[]{taskId},
                    messageSource
            );
        }

        // Set outcome to CANCELLED
        task.setOutcome("CANCELLED");
        Map<String, Object> outcomeDetails = new HashMap<>();
        outcomeDetails.put("cancelledAt", LocalDateTime.now());
        if (reason != null && !reason.trim().isEmpty()) {
            outcomeDetails.put("cancellationReason", reason);
        }
        task.setOutcomeDetails(outcomeDetails);

        // Save task - updatedBy from AuditableEntity will capture who cancelled it
        Task savedTask = taskRepository.save(task);
        log.info("Task cancelled successfully");

        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        return mapToTaskResponse(savedTask, taskConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        log.debug("Getting task by ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId, messageSource));
        TaskConfig taskConfig = getActiveTaskConfig(task.getTaskConfigKey());
        return mapToTaskResponse(task, taskConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByConfigKey(String taskConfigKey) {
        log.debug("Getting tasks by config key: {}", taskConfigKey);
        TaskConfig taskConfig = getActiveTaskConfig(taskConfigKey);
        List<Task> tasks = taskRepository.findByTaskConfigKey(taskConfigKey);
        return tasks.stream()
                .map(task -> mapToTaskResponse(task, taskConfig))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAssignedTo(String assignedTo) {
        log.debug("Getting tasks assigned to: {}", assignedTo);
        List<Task> tasks = taskRepository.findByAssignedTo(assignedTo);
        return tasks.stream()
                .map(this::mapToTaskResponseWithConfig)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAssignedToRole(String assignedToRole) {
        log.debug("Getting tasks assigned to role: {}", assignedToRole);
        List<Task> tasks = taskRepository.findByAssignedToRole(assignedToRole);
        return tasks.stream()
                .map(this::mapToTaskResponseWithConfig)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getPendingTasksByConfigKey(String taskConfigKey) {
        log.debug("Getting pending tasks for config key: {}", taskConfigKey);
        TaskConfig taskConfig = getActiveTaskConfig(taskConfigKey);
        List<Task> tasks = taskRepository.findByTaskConfigKeyAndOutcomeIsNull(taskConfigKey);
        return tasks.stream()
                .map(task -> mapToTaskResponse(task, taskConfig))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByOutcome(String outcome) {
        log.debug("Getting tasks with outcome: {}", outcome);
        List<Task> tasks = taskRepository.findByOutcome(outcome);
        return tasks.stream()
                .map(this::mapToTaskResponseWithConfig)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCompleteTask(Long taskId) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElse(null);
            return task != null && task.getOutcome() == null;
        } catch (Exception e) {
            log.error("Error checking if task can be completed: {}", taskId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidOutcome(String taskConfigKey, String outcome) {
        try {
            TaskConfig taskConfig = taskConfigRepository.findByTaskConfigKey(taskConfigKey)
                    .orElse(null);
            if (taskConfig == null || taskConfig.getTaskConfigDetails() == null) {
                return false;
            }

            Map<String, Object> configDetails = taskConfig.getTaskConfigDetails();
            if (configDetails.containsKey("allowedOutcomes")) {
                Object allowedOutcomes = configDetails.get("allowedOutcomes");
                if (allowedOutcomes instanceof List) {
                    return ((List<?>) allowedOutcomes).contains(outcome);
                }
            }

            // If no allowed outcomes configured, any outcome is valid
            return true;
        } catch (Exception e) {
            log.error("Error validating outcome for task config: {}", taskConfigKey, e);
            return false;
        }
    }

    // Private helper methods

    private TaskConfig getActiveTaskConfig(String taskConfigKey) {
        TaskConfig taskConfig = taskConfigRepository.findByTaskConfigKey(taskConfigKey)
                .orElseThrow(() -> new TaskConfigNotFoundException(taskConfigKey, messageSource));

        if (!Boolean.TRUE.equals(taskConfig.getIsActive())) {
            throw new TaskOperationException(
                    "error.task.config.inactive",
                    new Object[]{taskConfigKey},
                    messageSource
            );
        }

        return taskConfig;
    }

    private void validateCreateTaskRequest(CreateTaskRequest request) {
        if (!ValidationUtils.isNonNull(request)) {
            throw new TaskValidationException("error.task.request.required", null, messageSource);
        }
        if (!ValidationUtils.isNonNullOrEmpty(request.getTaskConfigKey())) {
            throw new TaskValidationException("error.task.config.key.required", null, messageSource);
        }
        if (!ValidationUtils.hasAtLeastOne(request.getAssignedTo(), request.getAssignedToRole())) {
            throw new TaskValidationException("error.task.assignment.required", null, messageSource);
        }
    }

    private void validateUpdateTaskRequest(UpdateTaskRequest request) {
        if (!ValidationUtils.isNonNull(request)) {
            throw new TaskValidationException("error.task.request.required", null, messageSource);
        }
        if (!ValidationUtils.isNonNull(request.getTaskId())) {
            throw new TaskValidationException("error.task.id.required", null, messageSource);
        }
        // At least one field should be provided for update
        if (!ValidationUtils.hasAtLeastOne(request.getAssignedTo(), request.getAssignedToRole(), 
                request.getDueAt(), request.getTaskDetails())) {
            throw new TaskValidationException("error.task.no.fields.to.update", null, messageSource);
        }
    }

    private void validateCompleteTaskRequest(CompleteTaskRequest request) {
        if (!ValidationUtils.isNonNull(request)) {
            throw new TaskValidationException("error.task.request.required", null, messageSource);
        }
        if (!ValidationUtils.isNonNull(request.getTaskId())) {
            throw new TaskValidationException("error.task.id.required", null, messageSource);
        }
        if (!ValidationUtils.isNonNullOrEmpty(request.getOutcome())) {
            throw new TaskValidationException("error.task.outcome.required", null, messageSource);
        }
    }

    private void validateOutcome(TaskConfig taskConfig, String outcome) {
        Map<String, Object> configDetails = taskConfig.getTaskConfigDetails();
        if (configDetails != null && configDetails.containsKey("allowedOutcomes")) {
            Object allowedOutcomes = configDetails.get("allowedOutcomes");
            if (allowedOutcomes instanceof List) {
                if (!((List<?>) allowedOutcomes).contains(outcome)) {
                    throw new TaskValidationException(
                            "error.task.outcome.invalid",
                            new Object[]{outcome, taskConfig.getTaskConfigKey()},
                            messageSource
                    );
                }
            }
        }
    }

    private TaskResponse mapToTaskResponse(Task task, TaskConfig taskConfig) {
        return TaskResponse.builder()
                .id(task.getId())
                .taskConfigKey(task.getTaskConfigKey())
                .taskName(taskConfig.getName())
                .taskDescription(taskConfig.getDescription())
                .assignedTo(task.getAssignedTo())
                .assignedToRole(task.getAssignedToRole())
                .dueAt(task.getDueAt())
                .outcome(task.getOutcome())
                .outcomeDetails(task.getOutcomeDetails())
                .taskDetails(task.getTaskDetails())
                .createdAt(task.getCreatedAt())
                .createdBy(task.getCreatedBy())
                .updatedAt(task.getUpdatedAt())
                .updatedBy(task.getUpdatedBy())
                .build();
    }

    private TaskResponse mapToTaskResponseWithConfig(Task task) {
        TaskConfig taskConfig = taskConfigRepository.findByTaskConfigKey(task.getTaskConfigKey())
                .orElse(null);
        return mapToTaskResponse(task, taskConfig);
    }
}

