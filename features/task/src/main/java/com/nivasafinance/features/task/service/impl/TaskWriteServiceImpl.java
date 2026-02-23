package com.nivasafinance.features.task.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.task.dto.BulkReassignTaskRequest;
import com.nivasafinance.features.task.dto.BulkReassignTaskResponse;
import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.dto.UpdateDueDateRequest;
import com.nivasafinance.features.task.dto.UpdateTaskNameRequest;
import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.entity.TaskConfig.TaskConfigDetails;
import com.nivasafinance.features.task.exception.TaskOperationException;
import com.nivasafinance.features.task.exception.TaskValidationException;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.task.repository.TaskRepositoryWrapper;
import com.nivasafinance.features.task.service.DueDateCalculatorService;
import com.nivasafinance.features.task.service.TaskConfigValidationService;
import com.nivasafinance.features.task.service.TaskEntityService;
import com.nivasafinance.features.task.service.TaskEntityServiceFactory;
import com.nivasafinance.features.task.service.TaskWriteService;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.TaskAssignedEventPayload;
import com.nivasafinance.common.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

    private static final String OUTCOME_RESCHEDULED = "RESCHEDULED";
    private static final String CONTEXT_ENTITY_ID = "entityId";
    private static final String CONTEXT_ENTITY_TYPE = "entityType";
    private static final String CONTEXT_ASSIGNED_TO = "assignedTo";

    private final TaskRepositoryWrapper taskRepositoryWrapper;
    private final TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;
    private final TaskConfigValidationService taskConfigValidationService;
    private final DueDateCalculatorService dueDateCalculatorService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TaskEntityServiceFactory taskEntityServiceFactory;

    @Override
    public TaskResponse createTask(CreateTaskRequest request) {
        validateCreateTaskRequest(request);
        TaskConfig taskConfig = getActiveTaskConfig(request.getTaskConfigKey());
        request.setDueAt(calculateDueDate(request, taskConfig));
        Task task = buildTaskFromRequest(request, taskConfig);
        Task savedTask = taskRepositoryWrapper.saveWithException(task);
        publishTaskAssignedIfAssigned(savedTask);
        return TaskResponse.from(savedTask, taskConfig, objectMapper);
    }

    @Override
    public TaskResponse createAdhocTask(CreateAdhocTaskRequest request) {
        validateCreateAdhocTaskRequest(request, request.getTaskConfigKey());
        LocalDateTime dueAt = request.getDueAt();
        if (!ValidationUtils.isNonNull(request.getDueAt())) {
            TaskConfig taskConfig = getActiveTaskConfig(request.getTaskConfigKey());
            CreateTaskRequest createTaskRequest = CreateTaskRequest.builder()
                    .taskConfigKey(request.getTaskConfigKey())
                    .assignedTo(request.getAssignedTo())
                    .taskDetails(request.getTaskDetails())
                    .build();
            dueAt = calculateDueDateFromConfig(taskConfig, createTaskRequest);
            request.setDueAt(dueAt);
        }
        CreateTaskRequest createTaskRequest = CreateTaskRequest.builder()
                .taskConfigKey(request.getTaskConfigKey())
                .assignedTo(request.getAssignedTo())
                .dueAt(dueAt)
                .taskDetails(request.getTaskDetails())
                .build();
        return createTask(createTaskRequest);
    }

    @Override
    public TaskResponse reassignTask(ReassignTaskRequest request) {
        Task task = validateReassignTaskRequest(request);
        updateTaskAssignment(task, request.getNewAssignedTo());
        Task savedTask = taskRepositoryWrapper.saveWithException(task);
        publishTaskAssignedIfAssigned(savedTask);
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
    public TaskResponse updateTaskName(UUID taskIdentifier, UpdateTaskNameRequest request) {
        Task task = validateUpdateTaskNameRequest(taskIdentifier, request);
        task.setName(request.getName());
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

        CreateTaskRequest createTaskRequest = changeTaskDetailsForReschedule(request, oldTask);

        Task newTask = buildTaskFromRequest(createTaskRequest, taskConfig);
        Task savedTask = taskRepositoryWrapper.saveWithException(newTask);
        publishTaskAssignedIfAssigned(savedTask);

        return TaskResponse.from(oldTask, taskConfig, objectMapper);
    }

    @Override
    public TaskResponse completeTask(CompleteTaskRequest request) {
        TaskValidationResult validationResult = validateCompleteTaskRequest(request);

        Task.OutcomeDetails outcomeDetails = buildOutcomeDetails(request);

        updateTaskOutcome(validationResult.task, request.getOutcomeCodeValueKey(), outcomeDetails);
        Task savedTask = taskRepositoryWrapper.saveWithException(validationResult.task);
        return TaskResponse.from(savedTask, validationResult.taskConfig, objectMapper);
    }

    private CreateTaskRequest changeTaskDetailsForReschedule(RescheduleTaskRequest request, Task oldTask) {
        Task.TaskDetails oldDetails = oldTask.getTaskDetails();
        TaskDetailsRequest newTaskDetails = null;
        if (ValidationUtils.isNonNull(oldDetails)) {
            Task.OutcomeDetails oldOutcome = oldTask.getOutcomeDetails();
            newTaskDetails = TaskDetailsRequest.builder()
                    .entityId(oldDetails.getEntityId())
                    .entityType(oldDetails.getEntityType())
                    .stageKey(oldDetails.getStageKey())
                    .preferredCallWindow(TaskDetailsRequest.PreferredCallWindow.builder()
                            .start(request.getPreferredStartTime())
                            .end(request.getPreferredEndTime())
                            .build())
                    .creatorRemarks(request.getCreatorRemarks())
                    .iterationCount(ValidationUtils.isNonNull(oldDetails.getIterationCount()) ? oldDetails.getIterationCount() + 1 : 1)
                    .rescheduledFromTaskIdentifier(oldTask.getTaskIdentifier())
                    .rescheduleReasonCodeValueKey(request.getReasonCodeValueKey())
                    .rescheduledFromTaskRemarks(oldOutcome != null ? oldOutcome.getRemarks() : null)
                    .build();
        } else {
            newTaskDetails = TaskDetailsRequest.builder()
                    .creatorRemarks(request.getCreatorRemarks())
                    .iterationCount(1)
                    .rescheduledFromTaskIdentifier(oldTask.getTaskIdentifier())
                    .rescheduleReasonCodeValueKey(request.getReasonCodeValueKey())
                    .preferredCallWindow(TaskDetailsRequest.PreferredCallWindow.builder()
                            .start(request.getPreferredStartTime())
                            .end(request.getPreferredEndTime())
                            .build())
                    .build();
        }
        return CreateTaskRequest.builder()
                .taskConfigKey(oldTask.getTaskConfigKey())
                .assignedTo(oldTask.getAssignedTo())
                .dueAt(request.getPreferredEndTime())
                .taskDetails(newTaskDetails)
                .build();
    }

    private Task.OutcomeDetails buildOutcomeDetails(CompleteTaskRequest request) {
        return Task.OutcomeDetails.builder()
                .remarks(ValidationUtils.isNonNull(request.getOutcomeDetails())
                        ? request.getOutcomeDetails().getRemarks()
                        : null)
                .locationDetails(ValidationUtils.isNonNull(request.getOutcomeDetails())
                        ? request.getOutcomeDetails().getLocationDetails()
                        : null)
                .completedAt(LocalDateTime.now())
                .completedBy(UserContext.getUsername())
                .build();
    }

    private void validateCreateTaskRequest(CreateTaskRequest request) {
        validateRequestNotNull(request);
        validateIfTaskConfigExistsAndIsActive(request.getTaskConfigKey());
        validateDueDate(request.getDueAt(), request.getTaskConfigKey());
        validateEntityDetails(request.getTaskDetails(), request.getTaskConfigKey());
    }

    private void validateEntityDetails(TaskDetailsRequest taskDetails, String taskConfigKey) {
        if (!ValidationUtils.isNonNull(taskDetails)) {
            return;
        }
        boolean hasEntityId = ValidationUtils.isNonNull(taskDetails.getEntityId());
        boolean hasEntityType = ValidationUtils.isNonNull(taskDetails.getEntityType());
        if (!hasEntityId && !hasEntityType) {
            return;
        }
        if (hasEntityId && !hasEntityType) {
            throw TaskValidationException.entityTypeRequired(messageSource);
        }
        if (hasEntityType && !hasEntityId) {
            throw TaskValidationException.entityIdRequired(messageSource);
        }
        TaskEntityService taskEntityService = taskEntityServiceFactory
                .getTaskEntityService(taskDetails.getEntityType());

        taskEntityService.validate(taskDetails.getEntityId());
    }

    private LocalDateTime calculateDueDate(CreateTaskRequest request, TaskConfig taskConfig) {
        LocalDateTime dueAt = request.getDueAt();
        if (!ValidationUtils.isNonNull(dueAt)) {
            dueAt = calculateDueDateFromConfig(taskConfig, request);
        }
        return dueAt;
    }

    private Task validateReassignTaskRequest(ReassignTaskRequest request) {
        validateRequestNotNull(request);
        Task task = getTask(request.getTaskIdentifier());
        validateTaskNotCompleted(task);
        validateAssignment(task, request.getNewAssignedTo());
        return task;
    }

    private Task validateRescheduleTaskRequest(RescheduleTaskRequest request) {
        validateRequestNotNull(request);
        Task task = getTask(request.getTaskIdentifier());
        validateTaskNotCompleted(task);
        validateDueDate(request.getPreferredEndTime(), task.getTaskConfigKey());
        return task;
    }

    private Task validateUpdateDueDateRequest(UpdateDueDateRequest request) {
        validateRequestNotNull(request);
        Task task = getTask(request.getTaskIdentifier());
        validateTaskNotCompleted(task);
        validateDueDate(request.getDueAt(), task.getTaskConfigKey());
        return task;
    }

    private Task validateUpdateTaskNameRequest(UUID taskIdentifier, UpdateTaskNameRequest request) {
        validateRequestNotNull(request);
        Task task = taskRepositoryWrapper.findByTaskIdentifierWithException(taskIdentifier);
        validateTaskNotCompleted(task);
        return task;
    }

    private TaskValidationResult validateCompleteTaskRequest(CompleteTaskRequest request) {
        validateRequestNotNull(request);
        Task task = getTask(request.getTaskIdentifier());
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

    private Task getTask(UUID taskIdentifier) {
        return taskRepositoryWrapper.findByTaskIdentifierWithException(taskIdentifier);
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

    private Task buildTaskFromRequest(CreateTaskRequest request, TaskConfig taskConfig) {
        Task task = new Task();
        task.setTaskConfigKey(request.getTaskConfigKey());
        task.setName(taskConfig != null ? taskConfig.getName() : null);
        task.setAssignedTo(request.getAssignedTo());
        task.setDueAt(request.getDueAt());

        configureTask(task, request, taskConfig);
        return task;
    }

    private void configureTask(Task task, CreateTaskRequest request, TaskConfig taskConfig) {
        task.setTaskConfigKey(request.getTaskConfigKey());
        task.setName(taskConfig != null ? taskConfig.getName() : null);
        task.setAssignedTo(request.getAssignedTo());
        task.setDueAt(request.getDueAt());
        task.setTaskDetails(buildTaskDetails(request));
    }

    private Task.TaskDetails buildTaskDetails(CreateTaskRequest request) {
        if (!ValidationUtils.isNonNull(request.getTaskDetails())) {
            return null;
        }
        TaskDetailsRequest details = request.getTaskDetails();
        return Task.TaskDetails.builder()
                .entityId(details.getEntityId())
                .entityType(details.getEntityType())
                .stageKey(details.getStageKey())
                .preferredCallWindow(buildPreferredCallWindow(details.getPreferredCallWindow()))
                .creatorRemarks(details.getCreatorRemarks())
                .iterationCount(
                        ValidationUtils.isNonNull(details.getIterationCount()) ? details.getIterationCount() : 0)
                .rescheduledFromTaskIdentifier(details.getRescheduledFromTaskIdentifier())
                .rescheduleReasonCodeValueKey(details.getRescheduleReasonCodeValueKey())
                .rescheduledFromTaskRemarks(details.getRescheduledFromTaskRemarks())
                .build();
    }

    private Task.TaskDetails.PreferredCallWindow buildPreferredCallWindow(
            TaskDetailsRequest.PreferredCallWindow preferredCallWindow) {
        if (preferredCallWindow == null) {
            return null;
        }
        LocalDateTime start = preferredCallWindow.getStart();
        LocalDateTime end = preferredCallWindow.getEnd();
        if (!ValidationUtils.isNonNull(start) || !ValidationUtils.isNonNull(end)) {
            return null;
        }
        return Task.TaskDetails.PreferredCallWindow.builder()
                .start(start)
                .end(end)
                .build();
    }

    private TaskConfig getActiveTaskConfig(String taskConfigKey) {
        return taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey);
    }

    private LocalDateTime calculateDueDateFromConfig(TaskConfig taskConfig, CreateTaskRequest request) {
        TaskConfigDetails details = taskConfig.getTaskConfigDetails();
        if (ValidationUtils.isNonNull(details)) {
            String expression = details.getDueDateLogicExpression();
            if (ValidationUtils.isNonNullOrEmpty(expression)) {
                return dueDateCalculatorService.calculateDueDate(expression, buildContext(request));
            }
        }
        return null;
    }

    private Map<String, Object> buildContext(CreateTaskRequest request) {
        Map<String, Object> context = new HashMap<>();
        if (ValidationUtils.isNonNull(request.getTaskDetails())) {
            if (ValidationUtils.isNonNull(request.getTaskDetails().getEntityId())) {
                context.put(CONTEXT_ENTITY_ID, request.getTaskDetails().getEntityId());
            }
            if (ValidationUtils.isNonNull(request.getTaskDetails().getEntityType())) {
                context.put(CONTEXT_ENTITY_TYPE, request.getTaskDetails().getEntityType());
            }
        }
        if (ValidationUtils.isNonNull(request.getAssignedTo())) {
            context.put(CONTEXT_ASSIGNED_TO, request.getAssignedTo());
        }
        return context;
    }

    private void updateTaskAssignment(Task task, String assignedTo) {
        task.setAssignedTo(assignedTo);
    }

    private void validateCreateAdhocTaskRequest(CreateAdhocTaskRequest request, String taskConfigKey) {
        validateRequestNotNull(request);
        if (!ValidationUtils.isNonNullOrEmpty(request.getTaskConfigKey())) {
            throw TaskValidationException.taskConfigKeyRequired(messageSource);
        }
        TaskDetailsRequest taskDetails = request.getTaskDetails();
        if (ValidationUtils.isNonNull(taskDetails)
                && ValidationUtils.isNonNull(taskDetails.getEntityType())
                && ValidationUtils.isNonNull(taskDetails.getEntityId())) {
            TaskEntityService taskEntityService = taskEntityServiceFactory
                    .getTaskEntityService(taskDetails.getEntityType());
            taskEntityService.canCreateAdhocTask(taskDetails, taskConfigKey);
        } else {
            checkIfAdhocTaskCanBeCreated(taskConfigKey);
        }
    }

    private void publishTaskAssignedIfAssigned(Task task) {
        String assignedTo = task.getAssignedTo();
        if (ValidationUtils.isNonNullOrEmpty(assignedTo)) {
            var details = task.getTaskDetails();
            TaskAssignedEventPayload payload = TaskAssignedEventPayload.builder()
                    .username(assignedTo)
                    .taskIdentifier(task.getTaskIdentifier())
                    .taskName(task.getName())
                    .taskConfigKey(task.getTaskConfigKey())
                    .entityType(details != null ? details.getEntityType() : null)
                    .entityId(details != null ? details.getEntityId() : null)
                    .build();
            applicationEventPublisher.publishEvent(
                    new SystemEvent<>(BusinessEvent.TASK_ASSIGNED.toString(), payload, assignedTo));
        }
    }

    private void closeTaskWithRescheduledOutcome(Task task, RescheduleTaskRequest request) {
        Task.OutcomeDetails outcomeDetails = Task.OutcomeDetails.builder()
                .rescheduleReasonCodeValueKey(request.getReasonCodeValueKey())
                .locationDetails(request.getLocationDetails())
                .build();
        updateTaskOutcome(task, OUTCOME_RESCHEDULED, outcomeDetails);
    }

    private void updateTaskOutcome(Task task, String outcome, Task.OutcomeDetails outcomeDetails) {
        task.setOutcome(outcome);
        task.setOutcomeDetails(outcomeDetails);
    }

    @Override
    public void closeAllOpenTasksForLead(UUID leadIdentifier, String outcome) {
        List<Task> openTasks = taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier);

        for (Task task : openTasks) {
            Task.OutcomeDetails outcomeDetails = buildOutcomeDetailsForClose(outcome);
            updateTaskOutcome(task, outcome, outcomeDetails);
            taskRepositoryWrapper.saveWithException(task);
        }
    }

    private Task.OutcomeDetails buildOutcomeDetailsForClose(String outcome) {
        return Task.OutcomeDetails.builder()
                .remarks(getRemarksForOutcome(outcome))
                .completedAt(LocalDateTime.now())
                .completedBy(UserContext.getUsername())
                .build();
    }

    private String getRemarksForOutcome(String outcome) {
        switch (outcome) {
            case "REJECTED":
                return "Lead got rejected";
            case "WITHDRAWN":
                return "Lead got withdrawn";
            case "DROPOFF":
                return "Lead got dropped off";
            case "CLOSED":
                return "Lead got closed";
            default:
                return "Lead status changed";
        }
    }

    @Override
    public BulkReassignTaskResponse bulkReassignTasks(BulkReassignTaskRequest request) {
        List<UUID> successfulTaskIdentifiers = new ArrayList<>();
        List<BulkReassignTaskResponse.BulkAssignmentError> errors = new ArrayList<>();

        for (UUID taskIdentifier : request.getTaskIdentifiers()) {
            try {
                reassignTask(buildReassignTaskRequest(taskIdentifier, request.getNewAssignedTo()));
                successfulTaskIdentifiers.add(taskIdentifier);
            } catch (Exception e) {
                addError(errors, taskIdentifier, e.getMessage());
            }
        }

        return buildBulkReassignTaskResponse(request, successfulTaskIdentifiers, errors);
    }

    private ReassignTaskRequest buildReassignTaskRequest(UUID taskIdentifier, String newAssignedTo) {
        return ReassignTaskRequest.builder()
                .taskIdentifier(taskIdentifier)
                .newAssignedTo(newAssignedTo)
                .build();
    }

    private BulkReassignTaskResponse buildBulkReassignTaskResponse(BulkReassignTaskRequest request,
            List<UUID> successfulTaskIdentifiers, List<BulkReassignTaskResponse.BulkAssignmentError> errors) {
        return BulkReassignTaskResponse.builder()
                .totalRequested(request.getTaskIdentifiers().size())
                .successful(successfulTaskIdentifiers.size())
                .failed(errors.size())
                .successfulTaskIdentifiers(successfulTaskIdentifiers)
                .errors(errors)
                .build();
    }

    private void addError(List<BulkReassignTaskResponse.BulkAssignmentError> errors, UUID taskIdentifier,
            String errorMessage) {
        errors.add(BulkReassignTaskResponse.BulkAssignmentError.builder()
                .taskIdentifier(taskIdentifier)
                .errorMessage(errorMessage)
                .build());
    }

    private static class TaskValidationResult {
        private final Task task;
        private final TaskConfig taskConfig;

        public TaskValidationResult(Task task, TaskConfig taskConfig) {
            this.task = task;
            this.taskConfig = taskConfig;
        }
    }

    private void checkIfAdhocTaskCanBeCreated(String taskConfigKey) {
        TaskConfig taskConfig = getActiveTaskConfig(taskConfigKey);
        if (taskConfig == null) {
            throw TaskValidationException.taskConfigKeyRequired(messageSource);
        }
        if (!taskConfig.getTaskConfigDetails().getIsAdhocTaskAllowed()) {
            throw TaskOperationException.adhocTaskNotAllowed(taskConfigKey, messageSource);
        }
    }
}
