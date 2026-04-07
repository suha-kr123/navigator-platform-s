package com.nivasafinance.features.workflow.orchestrator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bre.dto.BREExecutionRequest;
import com.nivasafinance.features.bre.dto.BREExecutionResponse;
import com.nivasafinance.features.bre.service.BREExecutionService;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.repository.StageConfigRepositoryWrapper;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.workflow.constants.WorkflowConstants;
import com.nivasafinance.features.workflow.dto.PostTaskAction;
import com.nivasafinance.features.workflow.dto.TaskCompletionRule;
import com.nivasafinance.features.workflow.dto.WorkflowConfigDto;
import com.nivasafinance.features.workflow.dto.WorkflowStageConfig;
import com.nivasafinance.features.workflow.entity.PendingWorkflowAction;
import com.nivasafinance.features.workflow.repository.PendingWorkflowActionRepositoryWrapper;
import com.nivasafinance.features.workflow.enums.TaskType;
import com.nivasafinance.features.workflow.exception.WorkflowConfigParseException;
import com.nivasafinance.features.workflow.exception.WorkflowConfigValidationException;
import com.nivasafinance.features.workflow.exception.WorkflowValidationException;
import com.nivasafinance.features.workflow.service.WorkflowConfigReadService;
import com.nivasafinance.features.workflow.adapter.EntityWorkflowAdapter;
import com.nivasafinance.features.workflow.adapter.EntityWorkflowAdapterRegistry;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkflowOrchestratorServiceImpl implements WorkflowOrchestratorService {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;
    private final WorkflowConfigReadService workflowConfigReadService;
    private final StageConfigRepositoryWrapper stageConfigRepositoryWrapper;
    private final CodeMasterService codeMasterService;
    private final EntityWorkflowAdapterRegistry adapterRegistry;
    private final BREExecutionService breExecutionService;
    private final PendingWorkflowActionRepositoryWrapper pendingActionRepositoryWrapper;
    
    public WorkflowOrchestratorServiceImpl(
            ObjectMapper objectMapper,
            MessageSource messageSource,
            TaskConfigRepositoryWrapper taskConfigRepositoryWrapper,
            WorkflowConfigReadService workflowConfigReadService,
            StageConfigRepositoryWrapper stageConfigRepositoryWrapper,
            CodeMasterService codeMasterService,
            @Lazy EntityWorkflowAdapterRegistry adapterRegistry,
            BREExecutionService breExecutionService,
            PendingWorkflowActionRepositoryWrapper pendingActionRepositoryWrapper) {
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
        this.taskConfigRepositoryWrapper = taskConfigRepositoryWrapper;
        this.workflowConfigReadService = workflowConfigReadService;
        this.stageConfigRepositoryWrapper = stageConfigRepositoryWrapper;
        this.codeMasterService = codeMasterService;
        this.adapterRegistry = adapterRegistry;
        this.breExecutionService = breExecutionService;
        this.pendingActionRepositoryWrapper = pendingActionRepositoryWrapper;
    }


    private WorkflowConfigDto parseWorkflowConfig(
            com.nivasafinance.features.workflow.entity.WorkflowConfig workflowConfig) {
        com.nivasafinance.features.workflow.entity.WorkflowConfig.WorkflowConfigDetails details = workflowConfig
                .getWorkflowConfigDetails();
        if (!ValidationUtils.isNonNull(details)) {
            throw WorkflowConfigValidationException.invalidWorkflowConfiguration(messageSource);
        }
        try {
            return objectMapper.convertValue(details, WorkflowConfigDto.class);
        } catch (Exception e) {
            throw WorkflowConfigParseException.failedToParse(messageSource);
        }
    }

    private WorkflowStageConfig findStageConfig(WorkflowConfigDto workflowConfigDto, String stageKey) {
        if (!ValidationUtils.isNonNull(workflowConfigDto.getStages())) {
            return null;
        }
        return workflowConfigDto.getStages().stream()
                .filter(stage -> stageKey.equals(stage.getStageKey()))
                .findFirst()
                .orElse(null);
    }

    private List<com.nivasafinance.features.workflow.dto.TaskConfig> determineTasksToCreate(
            WorkflowStageConfig stageConfig, String fromStageKey) {
        if (!ValidationUtils.isNonNull(stageConfig.getStageTasks())) {
            return List.of();
        }

        List<String> allTaskConfigKeys = stageConfig.getStageTasks().stream()
                .filter(stageTaskConfig -> stageTaskConfig.matches(fromStageKey))
                .filter(stageTaskConfig -> ValidationUtils.isNonNull(stageTaskConfig.getTaskConfigKeys()))
                .flatMap(stageTaskConfig -> stageTaskConfig.getTaskConfigKeys().stream())
                .distinct()
                .collect(Collectors.toList());

        if (!ValidationUtils.isNonNull(allTaskConfigKeys) || allTaskConfigKeys.isEmpty()) {
            return List.of();
        }

        Map<String, TaskConfig> taskConfigMap = taskConfigRepositoryWrapper
                .findActiveByTaskConfigKeys(allTaskConfigKeys);

        return allTaskConfigKeys.stream()
                .map(taskConfigMap::get)
                .filter(ValidationUtils::isNonNull)
                .map(this::buildTaskConfigFromEntity)
                .filter(taskConfig -> ValidationUtils.isNonNull(taskConfig) 
                        && (taskConfig.getTaskType() == TaskType.AUTOMATED || taskConfig.getTaskType() == TaskType.BOTH))
                .collect(Collectors.toList());
    }

    private com.nivasafinance.features.workflow.dto.TaskConfig buildTaskConfigFromEntity(TaskConfig taskConfigEntity) {
        TaskType taskType = TaskType.AUTOMATED;

        List<String> allowedRoles = null;
        if (ValidationUtils.isNonNull(taskConfigEntity.getTaskConfigDetails())
                && ValidationUtils.isNonNull(taskConfigEntity.getTaskConfigDetails().getAllowedRoles())) {
            allowedRoles = taskConfigEntity.getTaskConfigDetails().getAllowedRoles();
        }

        return com.nivasafinance.features.workflow.dto.TaskConfig.builder()
                .taskConfigKey(taskConfigEntity.getTaskConfigKey())
                .taskType(taskType)
                .allowedRoles(allowedRoles)
                .build();
    }


    private void createTasks(Long entityId, EntityType entityType, String stageKey,
            List<com.nivasafinance.features.workflow.dto.TaskConfig> tasksToCreate, Map<String, Object> context) {
        if (!ValidationUtils.isNonNull(tasksToCreate) || tasksToCreate.isEmpty()) {
            return;
        }

        // Extract context values directly
        String assignedTo = (String) context.get(WorkflowConstants.ContextKeys.ASSIGNED_TO);
        Object dueAtValue = context.get(WorkflowConstants.ContextKeys.DUE_AT);
        LocalDateTime dueAt = dueAtValue instanceof LocalDateTime ? (LocalDateTime) dueAtValue : null;

        // Build task details map inline
        Map<String, Object> taskDetails = Map.of(WorkflowConstants.TaskDetails.STAGE_KEY, stageKey);

        // Get adapter once and reuse
        EntityWorkflowAdapter adapter = adapterRegistry.getAdapter(entityType);
        
        // Get entity info once
        Object entityInfo = adapter.getEntity(entityId);
        TaskDetailsRequest.PreferredCallWindow preferredCallWindow = ValidationUtils.isNonNull(entityInfo) 
                ? adapter.getPreferredCallWindow(entityInfo) 
                : null;

        List<String> errors = new ArrayList<>();

        for (com.nivasafinance.features.workflow.dto.TaskConfig taskConfig : tasksToCreate) {
            try {
                // Pass dueAt as-is (can be null). Task service will calculate from task config if needed.
                CreateTaskRequest createTaskRequest = buildCreateTaskRequest(
                        taskConfig, entityId, entityType, stageKey, assignedTo, dueAt, preferredCallWindow, entityInfo, adapter);
                adapter.createTaskAndAssociate(entityId, createTaskRequest, taskDetails);
            } catch (Exception e) {
                String errorMessage = ValidationUtils.isNonNull(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
                errors.add(taskConfig.getTaskConfigKey() + ": " + errorMessage);
            }
        }

        if (ValidationUtils.isNonNull(errors) && !errors.isEmpty()) {
            throw new IllegalStateException("Some tasks failed to create for entity " + entityId + ": " + errors);
        }
    }

    private CreateTaskRequest buildCreateTaskRequest(
            com.nivasafinance.features.workflow.dto.TaskConfig taskConfig,
            Long entityId,
            EntityType entityType,
            String stageKey,
            String assignedTo,
            LocalDateTime dueAt,
            TaskDetailsRequest.PreferredCallWindow preferredCallWindow,
            Object entityInfo,
            EntityWorkflowAdapter adapter) {

        UUID entityIdentifier = ValidationUtils.isNonNull(entityInfo) && ValidationUtils.isNonNull(adapter)
                ? adapter.getEntityIdentifier(entityInfo)
                : null;

        TaskDetailsRequest taskDetails = TaskDetailsRequest.builder()
                .entityId(entityIdentifier)
                .entityType(entityType)
                .stageKey(stageKey)
                .creatorRemarks(null)
                .preferredCallWindow(preferredCallWindow)
                .build();

        return CreateTaskRequest.builder()
                .taskConfigKey(taskConfig.getTaskConfigKey())
                .assignedTo(assignedTo)
                .dueAt(dueAt)
                .taskDetails(taskDetails)
                .build();
    }



    @Override
    @Transactional
    public void createTasksForStageTransition(
            Long entityId,
            EntityType entityType,
            String fromStageKey,
            String toStageKey,
            String assignedTo,
            String workflowConfigKey,
            Map<String, Object> context) {
        
        // Input validation
        ValidationUtils.requireNonNull(entityId, WorkflowValidationException::nullEntityId);
        ValidationUtils.requireNonNull(entityType, WorkflowValidationException::nullEntityType);
        ValidationUtils.requireNonNullOrEmpty(toStageKey, WorkflowValidationException::nullOrEmptyToStageKey);
        ValidationUtils.requireNonNullOrEmpty(workflowConfigKey, WorkflowValidationException::nullOrEmptyWorkflowConfigKey);
        
        com.nivasafinance.features.workflow.entity.WorkflowConfig workflowConfig = workflowConfigReadService
                .getWorkflowConfigByKey(workflowConfigKey);
        
        WorkflowConfigDto workflowConfigDto = parseWorkflowConfig(workflowConfig);
        WorkflowStageConfig stageConfig = findStageConfig(workflowConfigDto, toStageKey);
        if (!ValidationUtils.isNonNull(stageConfig)) {
            log.warn("Stage config not found for stageKey: {} in workflow: {}. Skipping task creation.", 
                    toStageKey, workflowConfigKey);
            return;
        }

        List<com.nivasafinance.features.workflow.dto.TaskConfig> tasksToCreate = determineTasksToCreate(stageConfig,
                fromStageKey);

        if (!ValidationUtils.isNonNull(tasksToCreate) || tasksToCreate.isEmpty()) {
            log.debug("No tasks to create for stage transition: {} -> {} (entityId: {}, entityType: {})", 
                    fromStageKey, toStageKey, entityId, entityType);
            return;
        }

        createTasks(
                entityId,
                entityType,
                toStageKey,
                tasksToCreate,
                context);
    }

    @Override
    public void validateStageTransition(Long entityId, EntityType entityType, String stageKey, String assignedTo, boolean hasExistingHistory) {
        // Input validation
        ValidationUtils.requireNonNull(entityId, WorkflowValidationException::nullEntityId);
        ValidationUtils.requireNonNull(entityType, WorkflowValidationException::nullEntityType);
        ValidationUtils.requireNonNullOrEmpty(stageKey, WorkflowValidationException::nullOrEmptyStageKey);
        
        // Validation is generic - no entity-specific logic needed
        // Role validation removed - frontend API provides assignable users, audit trail provides accountability
        stageConfigRepositoryWrapper.findByKeyWithException(stageKey);

        // No role validation - any user can be assigned
        // assignedTo can be null for any stage transition
    }

    @Override
    public com.nivasafinance.features.workflow.dto.StageConfigResponse getStageConfig(String stageKey) {
        // Input validation
        ValidationUtils.requireNonNullOrEmpty(stageKey, WorkflowValidationException::nullOrEmptyStageKey);
        StageConfig stageConfig = stageConfigRepositoryWrapper.findByKeyWithException(stageKey);
        StageConfig.StageConfigDetails stageConfigDetails = stageConfig.getStageConfig();
        List<com.nivasafinance.features.workflow.dto.PossibleNextStage> possibleNextStages = ValidationUtils.isNonNull(stageConfigDetails)
                && ValidationUtils.isNonNull(stageConfigDetails.getPossibleNextStages())
                ? stageConfigDetails.getPossibleNextStages().stream()
                    .map(stage -> com.nivasafinance.features.workflow.dto.PossibleNextStage.builder()
                            .stageKey(stage.getStageKey())
                            .allowedRoles(stage.getAllowedRoles())
                            .build())
                    .collect(Collectors.toList())
                : Collections.emptyList();

        StageConfig.AssigneeRoles assigneeRolesEntity = stageConfig.getAssigneeRoles();
        List<String> assigneeRoles = ValidationUtils.isNonNull(assigneeRolesEntity) 
                && ValidationUtils.isNonNull(assigneeRolesEntity.getRoles())
                ? assigneeRolesEntity.getRoles() 
                : List.of();
        
        List<CodeValueResponse> subStages = fetchSubStages(stageConfig.getSubStagesCode());
        
        return com.nivasafinance.features.workflow.dto.StageConfigResponse.builder()
                .key(stageConfig.getKey())
                .name(stageConfig.getName())
                .description(stageConfig.getDescription())
                .possibleNextStages(possibleNextStages)
                .assigneeRoles(assigneeRoles)
                .subStages(subStages)
                .isActive(stageConfig.getIsActive())
                .createdAt(stageConfig.getCreatedAt())
                .createdBy(stageConfig.getCreatedBy())
                .updatedAt(stageConfig.getUpdatedAt())
                .updatedBy(stageConfig.getUpdatedBy())
                .build();
    }
    
    private List<CodeValueResponse> fetchSubStages(String subStagesCode) {
        if (!ValidationUtils.isNonNull(subStagesCode)) {
            return List.of();
        }
        return codeMasterService.getAllCodeValuesByCodeKey(subStagesCode, true, "default");
    }


    @Override
    public String getDefaultSubStageForStage(String workflowConfigKey, String stageKey) {
        // Input validation
        ValidationUtils.requireNonNullOrEmpty(workflowConfigKey, WorkflowValidationException::nullOrEmptyWorkflowConfigKey);
        ValidationUtils.requireNonNullOrEmpty(stageKey, WorkflowValidationException::nullOrEmptyStageKey);
        
        try {
            com.nivasafinance.features.workflow.entity.WorkflowConfig workflowConfig = 
                workflowConfigReadService.getWorkflowConfigByKey(workflowConfigKey);
            
            if (!ValidationUtils.isNonNull(workflowConfig) 
                    || !ValidationUtils.isNonNull(workflowConfig.getWorkflowConfigDetails())
                    || !ValidationUtils.isNonNull(workflowConfig.getWorkflowConfigDetails().getStages())) {
                return null;
            }
            
            // Find the stage config in the workflow
            WorkflowStageConfig stageConfig = workflowConfig.getWorkflowConfigDetails().getStages().stream()
                    .filter(stage -> stageKey.equals(stage.getStageKey()))
                    .findFirst()
                    .orElse(null);
            
            if (ValidationUtils.isNonNull(stageConfig)) {
                String defaultSubStage = stageConfig.getDefaultSubStage();
                if (ValidationUtils.isNonNullOrEmpty(defaultSubStage)) {
                    log.debug("Found default substage {} for stage {} in workflow {}", 
                            defaultSubStage, stageKey, workflowConfigKey);
                    return defaultSubStage;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get default substage for stage {} in workflow {}: {}", 
                    stageKey, workflowConfigKey, e.getMessage());
        }
        return null;
    }

    @Override
    public String getEntityWorkflowConfigKey(Long entityId, EntityType entityType) {
        // Input validation
        ValidationUtils.requireNonNull(entityId, WorkflowValidationException::nullEntityId);
        ValidationUtils.requireNonNull(entityType, WorkflowValidationException::nullEntityType);
        
        EntityWorkflowAdapter adapter = adapterRegistry.getAdapter(entityType);
        return adapter.getWorkflowConfigKey(entityId);
    }

    @Override
    @Transactional
    public void processTaskCompletion(
            UUID entityIdentifier,
            EntityType entityType,
            UUID sourceTaskIdentifier,
            String taskConfigKey,
            String outcome,
            String stageKey,
            String assignedTo) {

        EntityWorkflowAdapter adapter = adapterRegistry.getAdapter(entityType);
        Long entityId = adapter.resolveEntityId(entityIdentifier);

        String workflowConfigKey = adapter.getWorkflowConfigKey(entityId);
        if (!ValidationUtils.isNonNullOrEmpty(workflowConfigKey)) {
            log.warn("Workflow config key not found for entity: {} ({}). Skipping post-task actions.",
                    entityIdentifier, entityType);
            return;
        }

        com.nivasafinance.features.workflow.entity.WorkflowConfig workflowConfig =
                workflowConfigReadService.getWorkflowConfigByKey(workflowConfigKey);
        WorkflowConfigDto workflowConfigDto = parseWorkflowConfig(workflowConfig);

        WorkflowStageConfig stageConfig = findStageConfig(workflowConfigDto, stageKey);
        if (!ValidationUtils.isNonNull(stageConfig)
                || !ValidationUtils.isNonNull(stageConfig.getTaskCompletionRules())) {
            log.debug("No task completion rules for stage: {} in workflow: {}. Skipping.", stageKey, workflowConfigKey);
            return;
        }

        TaskCompletionRule rule = findMatchingTaskCompletionRule(stageConfig.getTaskCompletionRules(), taskConfigKey);
        if (!ValidationUtils.isNonNull(rule)) {
            log.debug("No task completion rule for task: {} in stage: {}. Skipping.", taskConfigKey, stageKey);
            return;
        }

        BREExecutionResponse breResponse = executeBRERule(rule.getBreRuleUname(),
                taskConfigKey, outcome, entityType, entityIdentifier, stageKey, assignedTo);
        if (!ValidationUtils.isNonNull(breResponse)) {
            return;
        }

        List<PostTaskAction> actions = parseActionsFromBREResponse(breResponse);
        if (actions.isEmpty()) {
            log.debug("No actions returned by BRE rule {} for task: {} with outcome: {}.",
                    rule.getBreRuleUname(), taskConfigKey, outcome);
            return;
        }

        processActions(actions, entityId, entityIdentifier, entityType, sourceTaskIdentifier,
                taskConfigKey, outcome, stageKey, adapter);
    }

    @Override
    @Transactional
    public void executePendingAction(UUID actionIdentifier, String assignTo, LocalDateTime dueDate) {
        PendingWorkflowAction pending = pendingActionRepositoryWrapper
                .findByActionIdentifierWithException(actionIdentifier);

        if (!PendingWorkflowAction.STATUS_PENDING.equals(pending.getStatus())) {
            throw WorkflowValidationException.actionAlreadyProcessed();
        }

        EntityWorkflowAdapter adapter = adapterRegistry.getAdapter(pending.getEntityType());
        Long entityId = adapter.resolveEntityId(pending.getEntityIdentifier());

        PendingWorkflowAction.ActionDetails details = pending.getActionDetails();
        PostTaskAction action = PostTaskAction.builder()
                .type(details.getType())
                .taskConfigKey(details.getTaskConfigKey())
                .targetStageKey(details.getTargetStageKey())
                .targetSubStageKey(details.getTargetSubStageKey())
                .assignTo(assignTo)
                .outcome(details.getOutcome())
                .dueDate(dueDate)
                .build();

        executeSingleAction(action, entityId, pending.getEntityIdentifier(),
                pending.getEntityType(), pending.getCurrentStageKey(), adapter);

        pending.setStatus(PendingWorkflowAction.STATUS_EXECUTED);
        pending.setExecutedAt(LocalDateTime.now());
        pending.setExecutedBy(com.nivasafinance.common.context.UserContext.getUsername());
        pending.getActionDetails().setAssignTo(assignTo);
        pending.getActionDetails().setDueDate(dueDate);
        pendingActionRepositoryWrapper.save(pending);
    }

    @Override
    @Transactional
    public void cancelPendingAction(UUID actionIdentifier) {
        PendingWorkflowAction pending = pendingActionRepositoryWrapper
                .findByActionIdentifierWithException(actionIdentifier);

        if (!PendingWorkflowAction.STATUS_PENDING.equals(pending.getStatus())) {
            throw WorkflowValidationException.actionAlreadyProcessed();
        }

        pending.setStatus(PendingWorkflowAction.STATUS_CANCELLED);
        pending.setExecutedAt(LocalDateTime.now());
        pending.setExecutedBy(com.nivasafinance.common.context.UserContext.getUsername());
        pendingActionRepositoryWrapper.save(pending);
    }

    @Override
    @Transactional
    public void cancelPendingActionsBySourceTask(UUID sourceTaskIdentifier) {
        List<PendingWorkflowAction> pendingActions = pendingActionRepositoryWrapper
                .findPendingBySourceTask(sourceTaskIdentifier);

        String cancelledBy = com.nivasafinance.common.context.UserContext.getUsername();
        LocalDateTime now = LocalDateTime.now();

        for (PendingWorkflowAction pending : pendingActions) {
            pending.setStatus(PendingWorkflowAction.STATUS_CANCELLED);
            pending.setExecutedAt(now);
            pending.setExecutedBy(cancelledBy);
            pendingActionRepositoryWrapper.save(pending);
        }
    }

    private TaskCompletionRule findMatchingTaskCompletionRule(List<TaskCompletionRule> rules, String taskConfigKey) {
        return rules.stream()
                .filter(r -> taskConfigKey.equals(r.getTaskConfigKey()))
                .findFirst()
                .orElse(null);
    }

    private BREExecutionResponse executeBRERule(String breRuleUname, String taskConfigKey, String outcome,
            EntityType entityType, UUID entityIdentifier, String stageKey, String assignedTo) {
        Map<String, Object> breContext = new HashMap<>();
        breContext.put("taskConfigKey", taskConfigKey);
        breContext.put("outcome", outcome);
        breContext.put("entityType", entityType.name());
        breContext.put("entityIdentifier", entityIdentifier.toString());
        breContext.put("stageKey", stageKey);
        breContext.put("assignedTo", assignedTo);

        BREExecutionRequest breRequest = BREExecutionRequest.builder().params(breContext).build();
        try {
            BREExecutionResponse breResponse = breExecutionService.execute(breRuleUname, breRequest).join();
            if (!ValidationUtils.isNonNull(breResponse) || ValidationUtils.isNonNullOrEmpty(breResponse.getError())) {
                log.error("BRE rule {} returned error: {}", breRuleUname,
                        breResponse != null ? breResponse.getError() : "null response");
                return null;
            }
            return breResponse;
        } catch (Exception e) {
            log.error("Failed to execute BRE rule {} for task completion: {}", breRuleUname, e.getMessage(), e);
            return null;
        }
    }

    private List<PostTaskAction> parseActionsFromBREResponse(BREExecutionResponse breResponse) {
        if (!ValidationUtils.isNonNull(breResponse.getResponse())) {
            return List.of();
        }
        Object actionsObj = breResponse.getResponse().get("actions");
        if (!ValidationUtils.isNonNull(actionsObj)) {
            return List.of();
        }
        try {
            return objectMapper.convertValue(actionsObj, new TypeReference<List<PostTaskAction>>() {});
        } catch (Exception e) {
            log.error("Failed to parse post-task actions from BRE response: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private void processActions(List<PostTaskAction> actions, Long entityId, UUID entityIdentifier,
            EntityType entityType, UUID sourceTaskIdentifier, String sourceTaskConfigKey,
            String sourceOutcome, String currentStageKey, EntityWorkflowAdapter adapter) {
        String effectiveStageKey = currentStageKey;

        for (PostTaskAction action : actions) {
            boolean autoExecute = Boolean.TRUE.equals(action.getAutoExecute());

            if (autoExecute) {
                try {
                    executeSingleAction(action, entityId, entityIdentifier, entityType,
                            effectiveStageKey, adapter);
                    if (PostTaskAction.TYPE_MOVE_STAGE.equals(action.getType())) {
                        effectiveStageKey = action.getTargetStageKey();
                    }
                } catch (Exception e) {
                    log.error("Failed to auto-execute post-task action {} for entity {}: {}",
                            action.getType(), entityIdentifier, e.getMessage(), e);
                }
            } else {
                storePendingAction(action, entityIdentifier, entityType, effectiveStageKey,
                        sourceTaskIdentifier, sourceTaskConfigKey, sourceOutcome);
            }
        }
    }

    private void executeSingleAction(PostTaskAction action, Long entityId, UUID entityIdentifier,
            EntityType entityType, String stageKey, EntityWorkflowAdapter adapter) {
        switch (action.getType()) {
            case PostTaskAction.TYPE_CREATE_TASK:
                executeCreateTaskAction(action, entityId, entityType, stageKey, adapter);
                break;
            case PostTaskAction.TYPE_MOVE_STAGE:
                adapter.transitionStage(entityIdentifier, stageKey,
                        action.getTargetStageKey(), action.getAssignTo());
                break;
            case PostTaskAction.TYPE_CLOSE_TASKS:
                String closeOutcome = ValidationUtils.isNonNullOrEmpty(action.getOutcome())
                        ? action.getOutcome() : "AUTO_CLOSED";
                adapter.closeOpenTasks(entityIdentifier, closeOutcome);
                break;
            case PostTaskAction.TYPE_CHANGE_SUBSTAGE:
                adapter.changeSubStage(entityIdentifier, stageKey, action.getTargetSubStageKey());
                break;
            default:
                log.warn("Unknown post-task action type: {}. Skipping.", action.getType());
        }
    }

    private void storePendingAction(PostTaskAction action, UUID entityIdentifier, EntityType entityType,
            String currentStageKey, UUID sourceTaskIdentifier, String sourceTaskConfigKey, String sourceOutcome) {
        PendingWorkflowAction pending = new PendingWorkflowAction();
        pending.setEntityIdentifier(entityIdentifier);
        pending.setEntityType(entityType);
        pending.setCurrentStageKey(currentStageKey);
        pending.setSourceTaskIdentifier(sourceTaskIdentifier);
        pending.setSourceTaskConfigKey(sourceTaskConfigKey);
        pending.setSourceOutcome(sourceOutcome);
        pending.setStatus(PendingWorkflowAction.STATUS_PENDING);
        pending.setActionDetails(PendingWorkflowAction.ActionDetails.builder()
                .type(action.getType())
                .taskConfigKey(action.getTaskConfigKey())
                .targetStageKey(action.getTargetStageKey())
                .targetSubStageKey(action.getTargetSubStageKey())
                .assignTo(action.getAssignTo())
                .outcome(action.getOutcome())
                .dueDate(action.getDueDate())
                .build());
        pendingActionRepositoryWrapper.save(pending);
    }

    private void executeCreateTaskAction(PostTaskAction action, Long entityId, EntityType entityType,
            String stageKey, EntityWorkflowAdapter adapter) {
        Object entityInfo = adapter.getEntity(entityId);
        UUID entityIdentifier = ValidationUtils.isNonNull(entityInfo)
                ? adapter.getEntityIdentifier(entityInfo) : null;
        TaskDetailsRequest.PreferredCallWindow preferredCallWindow = ValidationUtils.isNonNull(entityInfo)
                ? adapter.getPreferredCallWindow(entityInfo) : null;

        TaskDetailsRequest taskDetails = TaskDetailsRequest.builder()
                .entityId(entityIdentifier)
                .entityType(entityType)
                .stageKey(stageKey)
                .preferredCallWindow(preferredCallWindow)
                .build();

        CreateTaskRequest createTaskRequest = CreateTaskRequest.builder()
                .taskConfigKey(action.getTaskConfigKey())
                .assignedTo(action.getAssignTo())
                .taskDetails(taskDetails)
                .build();

        adapter.createTaskAndAssociate(entityId, createTaskRequest,
                Map.of(WorkflowConstants.TaskDetails.STAGE_KEY, stageKey));
    }
}
