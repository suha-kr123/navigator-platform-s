package com.nivasafinance.features.workflow.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;
import com.nivasafinance.common.exception.ForbiddenException;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.repository.StageConfigRepositoryWrapper;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.workflow.constants.WorkflowConstants;
import com.nivasafinance.features.workflow.dto.WorkflowConfigDto;
import com.nivasafinance.features.workflow.dto.WorkflowStageConfig;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    
    public WorkflowOrchestratorServiceImpl(
            ObjectMapper objectMapper,
            MessageSource messageSource,
            TaskConfigRepositoryWrapper taskConfigRepositoryWrapper,
            WorkflowConfigReadService workflowConfigReadService,
            StageConfigRepositoryWrapper stageConfigRepositoryWrapper,
            CodeMasterService codeMasterService,
            @Lazy EntityWorkflowAdapterRegistry adapterRegistry) {
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
        this.taskConfigRepositoryWrapper = taskConfigRepositoryWrapper;
        this.workflowConfigReadService = workflowConfigReadService;
        this.stageConfigRepositoryWrapper = stageConfigRepositoryWrapper;
        this.codeMasterService = codeMasterService;
        this.adapterRegistry = adapterRegistry;
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
                        taskConfig, entityId, entityType, assignedTo, dueAt, preferredCallWindow, entityInfo, adapter);
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
    @Transactional
    public Object createAdhocTaskForStage(Long entityId, EntityType entityType, String stageKey, String taskConfigKey) {
        // Input validation
        ValidationUtils.requireNonNull(entityId, WorkflowValidationException::nullEntityId);
        ValidationUtils.requireNonNull(entityType, WorkflowValidationException::nullEntityType);
        ValidationUtils.requireNonNullOrEmpty(stageKey, WorkflowValidationException::nullOrEmptyStageKey);
        ValidationUtils.requireNonNullOrEmpty(taskConfigKey, WorkflowValidationException::nullOrEmptyTaskConfigKey);
        
        // Get adapter once and reuse
        EntityWorkflowAdapter adapter = adapterRegistry.getAdapter(entityType);

        Object entity = adapter.getEntity(entityId);
        if (!ValidationUtils.isNonNull(entity)) {
            throw new IllegalStateException("Entity not found: " + entityId);
        }

        String workflowConfigKey = adapter.getWorkflowConfigKey(entityId);
        if (!ValidationUtils.isNonNull(workflowConfigKey)) {
            throw new IllegalStateException("Workflow config key not found for entity: " + entityId);
        }

        com.nivasafinance.features.workflow.entity.WorkflowConfig workflowConfig = workflowConfigReadService
                .getWorkflowConfigByKey(workflowConfigKey);

        validateAdhocTaskForStage(workflowConfig, stageKey, taskConfigKey);

        UUID entityIdentifier = adapter.getEntityIdentifier(entity);
        Object createAdhocTaskRequest = adapter.createAdhocTaskRequest(taskConfigKey, stageKey);
        return adapter.createAdhocTask(entityIdentifier, createAdhocTaskRequest);
    }

    private void validateAdhocTaskForStage(
            com.nivasafinance.features.workflow.entity.WorkflowConfig workflowConfig,
            String stageKey,
            String taskConfigKey) {
        WorkflowConfigDto workflowConfigDto = parseWorkflowConfig(workflowConfig);

        WorkflowStageConfig stageConfig = findStageConfig(workflowConfigDto, stageKey);

        if (!ValidationUtils.isNonNull(stageConfig)) {
            throw WorkflowConfigValidationException.stageNotFoundInWorkflow(stageKey,
                    workflowConfig.getWorkflowConfigKey(), messageSource);
        }

        List<String> allowedAdhocTasks = stageConfig.getAllowedAdhocTasks();

        if (!ValidationUtils.isNonNull(allowedAdhocTasks) || !allowedAdhocTasks.contains(taskConfigKey)) {
            throw WorkflowConfigValidationException.adhocTaskNotAllowedForStage(stageKey, taskConfigKey, messageSource);
        }

        taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey);
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

        // Allow null assignedTo for initial stage assignments (no previous history)
        if (!ValidationUtils.isNonNull(assignedTo)) {
            if (hasExistingHistory) {
                throw new ForbiddenException("User assignment required for stage transition");
            }
            return;
        }
        // No role validation - any user can be assigned
    }

    @Override
    public com.nivasafinance.features.workflow.dto.StageConfigResponse getStageConfig(String stageKey) {
        // Input validation
        ValidationUtils.requireNonNullOrEmpty(stageKey, WorkflowValidationException::nullOrEmptyStageKey);
        StageConfig stageConfig = stageConfigRepositoryWrapper.findByKeyWithException(stageKey);
        StageConfig.StageConfigDetails stageConfigDetails = stageConfig.getStageConfig();
        List<String> possibleNextStages = ValidationUtils.isNonNull(stageConfigDetails) 
                && ValidationUtils.isNonNull(stageConfigDetails.getPossibleNextStages())
                ? stageConfigDetails.getPossibleNextStages() 
                : List.of();
        
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


    @Override
    public List<String> getAdhocTaskKeysForStage(String workflowConfigKey, String stageKey) {
        // Input validation
        ValidationUtils.requireNonNullOrEmpty(workflowConfigKey, WorkflowValidationException::nullOrEmptyWorkflowConfigKey);
        ValidationUtils.requireNonNullOrEmpty(stageKey, WorkflowValidationException::nullOrEmptyStageKey);
        com.nivasafinance.features.workflow.entity.WorkflowConfig workflowConfig = workflowConfigReadService
                .getWorkflowConfigByKey(workflowConfigKey);

        if (!ValidationUtils.isNonNull(workflowConfig) 
                || !ValidationUtils.isNonNull(workflowConfig.getWorkflowConfigDetails())
                || !ValidationUtils.isNonNull(workflowConfig.getWorkflowConfigDetails().getStages())) {
            return new ArrayList<>();
        }

        Optional<WorkflowStageConfig> stageConfigOpt = workflowConfig.getWorkflowConfigDetails().getStages().stream()
                .filter(stage -> stageKey.equals(stage.getStageKey()))
                .findFirst();

        if (stageConfigOpt.isEmpty()) {
            return new ArrayList<>();
        }

        WorkflowStageConfig stageConfig = stageConfigOpt.get();
        List<String> allowedAdhocTaskKeys = stageConfig.getAllowedAdhocTasks();

        if (!ValidationUtils.isNonNull(allowedAdhocTaskKeys) || allowedAdhocTaskKeys.isEmpty()) {
            return new ArrayList<>();
        }

        return allowedAdhocTaskKeys;
    }

    private List<CodeValueResponse> fetchSubStages(String subStagesCode) {
        if (!ValidationUtils.isNonNull(subStagesCode)) {
            return List.of();
        }
        return codeMasterService.getAllCodeValuesByCodeKey(subStagesCode, true);
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
}
