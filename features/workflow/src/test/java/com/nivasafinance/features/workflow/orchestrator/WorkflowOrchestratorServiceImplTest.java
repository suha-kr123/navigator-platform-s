package com.nivasafinance.features.workflow.orchestrator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.bre.dto.BREExecutionRequest;
import com.nivasafinance.features.bre.dto.BREExecutionResponse;
import com.nivasafinance.features.bre.service.BREExecutionService;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.repository.StageConfigRepositoryWrapper;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.workflow.adapter.EntityWorkflowAdapter;
import com.nivasafinance.features.workflow.adapter.EntityWorkflowAdapterRegistry;
import com.nivasafinance.features.workflow.dto.PostTaskAction;
import com.nivasafinance.features.workflow.dto.StageConfigResponse;
import com.nivasafinance.features.workflow.dto.StageTaskConfig;
import com.nivasafinance.features.workflow.dto.TaskCompletionRule;
import com.nivasafinance.features.workflow.dto.WorkflowConfigDto;
import com.nivasafinance.features.workflow.dto.WorkflowStageConfig;
import com.nivasafinance.features.workflow.entity.PendingWorkflowAction;
import com.nivasafinance.features.workflow.entity.WorkflowConfig;
import com.nivasafinance.features.workflow.exception.WorkflowConfigParseException;
import com.nivasafinance.features.workflow.exception.WorkflowConfigValidationException;
import com.nivasafinance.features.workflow.exception.WorkflowValidationException;
import com.nivasafinance.features.workflow.repository.PendingWorkflowActionRepositoryWrapper;
import com.nivasafinance.features.workflow.service.WorkflowConfigReadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowOrchestratorServiceImplTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private MessageSource messageSource;
    @Mock private TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;
    @Mock private WorkflowConfigReadService workflowConfigReadService;
    @Mock private StageConfigRepositoryWrapper stageConfigRepositoryWrapper;
    @Mock private CodeMasterService codeMasterService;
    @Mock private EntityWorkflowAdapterRegistry adapterRegistry;
    @Mock private BREExecutionService breExecutionService;
    @Mock private PendingWorkflowActionRepositoryWrapper pendingActionRepositoryWrapper;

    @InjectMocks
    private WorkflowOrchestratorServiceImpl service;

    private UUID entityIdentifier;
    private UUID taskIdentifier;

    @BeforeEach
    void setUp() {
        entityIdentifier = UUID.randomUUID();
        taskIdentifier = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        UserContext.setUsername(null);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  createTasksForStageTransition: validation
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void createTasksForStageTransition_withNullEntityId_throwsValidationException() {
        assertThrows(WorkflowValidationException.class,
                () -> service.createTasksForStageTransition(null, EntityType.LEAD, "FROM", "TO", "user", "WF", Map.of()),
                "Null entityId should throw WorkflowValidationException");
    }

    @Test
    void createTasksForStageTransition_withNullEntityType_throwsValidationException() {
        assertThrows(WorkflowValidationException.class,
                () -> service.createTasksForStageTransition(1L, null, "FROM", "TO", "user", "WF", Map.of()),
                "Null entityType should throw WorkflowValidationException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createTasksForStageTransition_withInvalidToStageKey_throwsValidationException(String toStageKey) {
        assertThrows(WorkflowValidationException.class,
                () -> service.createTasksForStageTransition(1L, EntityType.LEAD, "FROM", toStageKey, "user", "WF", Map.of()),
                "Null or empty toStageKey should throw WorkflowValidationException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createTasksForStageTransition_withInvalidWorkflowConfigKey_throwsValidationException(String wfKey) {
        assertThrows(WorkflowValidationException.class,
                () -> service.createTasksForStageTransition(1L, EntityType.LEAD, "FROM", "TO", "user", wfKey, Map.of()),
                "Null or empty workflowConfigKey should throw WorkflowValidationException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  createTasksForStageTransition: parseWorkflowConfig
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void createTasksForStageTransition_whenConfigDetailsNull_throwsConfigValidationException() {
        WorkflowConfig wfConfig = new WorkflowConfig();
        wfConfig.setWorkflowConfigDetails(null);
        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(wfConfig);

        assertThrows(WorkflowConfigValidationException.class,
                () -> service.createTasksForStageTransition(1L, EntityType.LEAD, "FROM", "TO", "user", "WF", Map.of()),
                "Null workflow config details should throw WorkflowConfigValidationException");
    }

    @Test
    void createTasksForStageTransition_whenObjectMapperFails_throwsParseException() {
        WorkflowConfig wfConfig = buildWorkflowConfigEntity();
        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(wfConfig);
        when(objectMapper.convertValue(any(), eq(WorkflowConfigDto.class)))
                .thenThrow(new IllegalArgumentException("bad"));

        assertThrows(WorkflowConfigParseException.class,
                () -> service.createTasksForStageTransition(1L, EntityType.LEAD, "FROM", "TO", "user", "WF", Map.of()),
                "ObjectMapper conversion failure should throw WorkflowConfigParseException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  createTasksForStageTransition: early returns
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void createTasksForStageTransition_whenStageConfigNotFound_skipsTaskCreation() {
        WorkflowConfig wfConfig = buildWorkflowConfigEntity();
        WorkflowConfigDto wfDto = WorkflowConfigDto.builder()
                .stages(List.of(WorkflowStageConfig.builder().stageKey("OTHER_STAGE").build()))
                .build();
        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(wfConfig);
        when(objectMapper.convertValue(any(), eq(WorkflowConfigDto.class))).thenReturn(wfDto);

        service.createTasksForStageTransition(1L, EntityType.LEAD, "FROM", "TO", "user", "WF", Map.of());

        verifyNoInteractions(adapterRegistry);
    }

    @Test
    void createTasksForStageTransition_whenNoTasksToCreate_skipsTaskCreation() {
        WorkflowConfig wfConfig = buildWorkflowConfigEntity();
        WorkflowStageConfig stageConfig = WorkflowStageConfig.builder().stageKey("TO").stageTasks(null).build();
        WorkflowConfigDto wfDto = WorkflowConfigDto.builder().stages(List.of(stageConfig)).build();
        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(wfConfig);
        when(objectMapper.convertValue(any(), eq(WorkflowConfigDto.class))).thenReturn(wfDto);

        service.createTasksForStageTransition(1L, EntityType.LEAD, "FROM", "TO", "user", "WF", Map.of());

        verifyNoInteractions(adapterRegistry);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  createTasksForStageTransition: happy path & errors
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void createTasksForStageTransition_withValidConfig_createsTasksSuccessfully() {
        stubWorkflowWithOneTask();
        EntityWorkflowAdapter adapter = stubAdapterWithEntity();

        Map<String, Object> context = new HashMap<>();
        context.put("assignedTo", "user1");
        context.put("dueAt", LocalDateTime.of(2026, 5, 1, 10, 0));

        service.createTasksForStageTransition(1L, EntityType.LEAD, "FROM", "TO", "user", "WF", context);

        verify(adapter).createTaskAndAssociate(eq(1L), any(CreateTaskRequest.class), anyMap());
    }

    @Test
    void createTasksForStageTransition_whenTaskCreationFails_throwsIllegalStateException() {
        stubWorkflowWithOneTask();
        EntityWorkflowAdapter adapter = stubAdapterWithEntity();
        when(adapter.createTaskAndAssociate(eq(1L), any(), anyMap()))
                .thenThrow(new RuntimeException("task creation failed"));

        assertThrows(IllegalStateException.class,
                () -> service.createTasksForStageTransition(1L, EntityType.LEAD, "FROM", "TO", "user", "WF", Map.of()),
                "Failed task creation should throw IllegalStateException with error details");
    }

    @Test
    void createTasksForStageTransition_whenEntityInfoNull_stillCreatesTasks() {
        stubWorkflowWithOneTask();
        EntityWorkflowAdapter adapter = mock(EntityWorkflowAdapter.class);
        when(adapterRegistry.getAdapter(EntityType.LEAD)).thenReturn(adapter);
        when(adapter.getEntity(1L)).thenReturn(null);

        service.createTasksForStageTransition(1L, EntityType.LEAD, "FROM", "TO", "user", "WF", Map.of());

        verify(adapter).createTaskAndAssociate(eq(1L), any(CreateTaskRequest.class), anyMap());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  validateStageTransition
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void validateStageTransition_withNullEntityId_throwsValidationException() {
        assertThrows(WorkflowValidationException.class,
                () -> service.validateStageTransition(null, EntityType.LEAD, "STAGE", "user", false),
                "Null entityId should throw WorkflowValidationException");
    }

    @Test
    void validateStageTransition_withNullEntityType_throwsValidationException() {
        assertThrows(WorkflowValidationException.class,
                () -> service.validateStageTransition(1L, null, "STAGE", "user", false),
                "Null entityType should throw WorkflowValidationException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validateStageTransition_withInvalidStageKey_throwsValidationException(String stageKey) {
        assertThrows(WorkflowValidationException.class,
                () -> service.validateStageTransition(1L, EntityType.LEAD, stageKey, "user", false),
                "Null or empty stageKey should throw WorkflowValidationException");
    }

    @Test
    void validateStageTransition_withValidInputs_callsStageConfigLookup() {
        when(stageConfigRepositoryWrapper.findByKeyWithException("STAGE")).thenReturn(new StageConfig());

        assertDoesNotThrow(() -> service.validateStageTransition(1L, EntityType.LEAD, "STAGE", "user", true),
                "Valid inputs should not throw any exception");
        verify(stageConfigRepositoryWrapper).findByKeyWithException("STAGE");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getStageConfig
    // ═══════════════════════════════════════════════════════════════════

    @ParameterizedTest
    @NullAndEmptySource
    void getStageConfig_withInvalidStageKey_throwsValidationException(String stageKey) {
        assertThrows(WorkflowValidationException.class, () -> service.getStageConfig(stageKey),
                "Null or empty stageKey should throw WorkflowValidationException");
    }

    @Test
    void getStageConfig_withFullDetails_returnsMappedResponse() {
        StageConfig stageConfig = buildStageConfigWithDetails();
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(stageConfig);
        when(codeMasterService.getAllCodeValuesByCodeKey("SUB_CODE", true, "default"))
                .thenReturn(List.of(new CodeValueResponse()));

        StageConfigResponse response = service.getStageConfig("STG");

        assertEquals("STG", response.getKey(), "Response key should match the stage config key");
        assertEquals("Stage Name", response.getName(), "Response name should match the stage config name");
        assertEquals(1, response.getPossibleNextStages().size(),
                "Should have one possible next stage from config");
        assertEquals(List.of("ROLE_B"), response.getAssigneeRoles(),
                "Assignee roles should come from stage config");
        assertEquals(1, response.getSubStages().size(), "Should have sub stages from code master");
    }

    @Test
    void getStageConfig_whenStageConfigDetailsNull_returnsEmptyCollections() {
        StageConfig stageConfig = new StageConfig();
        stageConfig.setKey("STG");
        stageConfig.setStageConfig(null);
        stageConfig.setAssigneeRoles(null);
        stageConfig.setSubStagesCode(null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(stageConfig);

        StageConfigResponse response = service.getStageConfig("STG");

        assertTrue(response.getPossibleNextStages().isEmpty(),
                "Possible next stages should be empty when stage config details are null");
        assertTrue(response.getAssigneeRoles().isEmpty(),
                "Assignee roles should be empty when assigneeRoles is null");
        assertTrue(response.getSubStages().isEmpty(),
                "Sub stages should be empty when subStagesCode is null");
    }

    @Test
    void getStageConfig_whenNestedFieldsNull_returnsEmptyCollections() {
        StageConfig stageConfig = new StageConfig();
        stageConfig.setKey("STG");
        stageConfig.setStageConfig(StageConfig.StageConfigDetails.builder().possibleNextStages(null).build());
        stageConfig.setAssigneeRoles(StageConfig.AssigneeRoles.builder().roles(null).build());
        stageConfig.setSubStagesCode(null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(stageConfig);

        StageConfigResponse response = service.getStageConfig("STG");

        assertTrue(response.getPossibleNextStages().isEmpty(),
                "Possible next stages should be empty when the nested list is null");
        assertTrue(response.getAssigneeRoles().isEmpty(),
                "Assignee roles should be empty when roles list is null");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getDefaultSubStageForStage
    // ═══════════════════════════════════════════════════════════════════

    @ParameterizedTest
    @NullAndEmptySource
    void getDefaultSubStageForStage_withInvalidWorkflowConfigKey_throwsValidationException(String wfKey) {
        assertThrows(WorkflowValidationException.class,
                () -> service.getDefaultSubStageForStage(wfKey, "STAGE"),
                "Null or empty workflowConfigKey should throw WorkflowValidationException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getDefaultSubStageForStage_withInvalidStageKey_throwsValidationException(String stageKey) {
        assertThrows(WorkflowValidationException.class,
                () -> service.getDefaultSubStageForStage("WF", stageKey),
                "Null or empty stageKey should throw WorkflowValidationException");
    }

    @Test
    void getDefaultSubStageForStage_whenWorkflowConfigNull_returnsNull() {
        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(null);

        assertNull(service.getDefaultSubStageForStage("WF", "STAGE"),
                "Should return null when workflow config is null");
    }

    @Test
    void getDefaultSubStageForStage_whenConfigDetailsNull_returnsNull() {
        WorkflowConfig config = new WorkflowConfig();
        config.setWorkflowConfigDetails(null);
        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(config);

        assertNull(service.getDefaultSubStageForStage("WF", "STAGE"),
                "Should return null when workflow config details are null");
    }

    @Test
    void getDefaultSubStageForStage_whenStagesNull_returnsNull() {
        WorkflowConfig config = new WorkflowConfig();
        config.setWorkflowConfigDetails(WorkflowConfig.WorkflowConfigDetails.builder().stages(null).build());
        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(config);

        assertNull(service.getDefaultSubStageForStage("WF", "STAGE"),
                "Should return null when stages list is null");
    }

    @Test
    void getDefaultSubStageForStage_whenStageNotFound_returnsNull() {
        WorkflowConfig config = new WorkflowConfig();
        config.setWorkflowConfigDetails(WorkflowConfig.WorkflowConfigDetails.builder()
                .stages(List.of(WorkflowStageConfig.builder().stageKey("OTHER").build())).build());
        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(config);

        assertNull(service.getDefaultSubStageForStage("WF", "STAGE"),
                "Should return null when the requested stage is not found in workflow");
    }

    @Test
    void getDefaultSubStageForStage_whenDefaultSubStageNull_returnsNull() {
        stubWorkflowWithStage("STAGE", null);

        assertNull(service.getDefaultSubStageForStage("WF", "STAGE"),
                "Should return null when default substage is null");
    }

    @Test
    void getDefaultSubStageForStage_whenDefaultSubStageEmpty_returnsNull() {
        stubWorkflowWithStage("STAGE", "");

        assertNull(service.getDefaultSubStageForStage("WF", "STAGE"),
                "Should return null when default substage is empty string");
    }

    @Test
    void getDefaultSubStageForStage_whenDefaultSubStagePresent_returnsSubStage() {
        stubWorkflowWithStage("STAGE", "SUB_1");

        assertEquals("SUB_1", service.getDefaultSubStageForStage("WF", "STAGE"),
                "Should return the configured default substage for the stage");
    }

    @Test
    void getDefaultSubStageForStage_whenExceptionThrown_returnsNull() {
        when(workflowConfigReadService.getWorkflowConfigByKey("WF"))
                .thenThrow(new RuntimeException("not found"));

        assertNull(service.getDefaultSubStageForStage("WF", "STAGE"),
                "Should return null when an exception occurs during lookup");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getEntityWorkflowConfigKey
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getEntityWorkflowConfigKey_withNullEntityId_throwsValidationException() {
        assertThrows(WorkflowValidationException.class,
                () -> service.getEntityWorkflowConfigKey(null, EntityType.LEAD),
                "Null entityId should throw WorkflowValidationException");
    }

    @Test
    void getEntityWorkflowConfigKey_withNullEntityType_throwsValidationException() {
        assertThrows(WorkflowValidationException.class,
                () -> service.getEntityWorkflowConfigKey(1L, null),
                "Null entityType should throw WorkflowValidationException");
    }

    @Test
    void getEntityWorkflowConfigKey_withValidInputs_returnsKey() {
        EntityWorkflowAdapter adapter = mock(EntityWorkflowAdapter.class);
        when(adapterRegistry.getAdapter(EntityType.LEAD)).thenReturn(adapter);
        when(adapter.getWorkflowConfigKey(1L)).thenReturn("WF_KEY");

        assertEquals("WF_KEY", service.getEntityWorkflowConfigKey(1L, EntityType.LEAD),
                "Should return the workflow config key from the entity adapter");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  processTaskCompletion: early returns
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void processTaskCompletion_whenWorkflowConfigKeyNull_skipsProcessing() {
        EntityWorkflowAdapter adapter = stubAdapterForTaskCompletion(null);

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verifyNoInteractions(workflowConfigReadService);
    }

    @Test
    void processTaskCompletion_whenWorkflowConfigKeyEmpty_skipsProcessing() {
        EntityWorkflowAdapter adapter = stubAdapterForTaskCompletion("");

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verifyNoInteractions(workflowConfigReadService);
    }

    @Test
    void processTaskCompletion_whenStageConfigNull_skipsProcessing() {
        stubAdapterForTaskCompletion("WF");
        stubParsedWorkflowWithStages(List.of(WorkflowStageConfig.builder().stageKey("OTHER").build()));

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verifyNoInteractions(breExecutionService);
    }

    @Test
    void processTaskCompletion_whenNoTaskCompletionRules_skipsProcessing() {
        stubAdapterForTaskCompletion("WF");
        stubParsedWorkflowWithStages(List.of(
                WorkflowStageConfig.builder().stageKey("STAGE").taskCompletionRules(null).build()));

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verifyNoInteractions(breExecutionService);
    }

    @Test
    void processTaskCompletion_whenNoMatchingRule_skipsProcessing() {
        stubAdapterForTaskCompletion("WF");
        TaskCompletionRule rule = TaskCompletionRule.builder().taskConfigKey("OTHER_TASK").breRuleUname("BRE").build();
        stubParsedWorkflowWithStages(List.of(
                WorkflowStageConfig.builder().stageKey("STAGE").taskCompletionRules(List.of(rule)).build()));

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verifyNoInteractions(breExecutionService);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  processTaskCompletion: BRE edge cases
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void processTaskCompletion_whenBREReturnsNull_skipsActions() {
        stubFullTaskCompletionFlow();
        when(breExecutionService.execute(eq("BRE_1"), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(pendingActionRepositoryWrapper, never()).save(any());
    }

    @Test
    void processTaskCompletion_whenBREReturnsError_skipsActions() {
        stubFullTaskCompletionFlow();
        BREExecutionResponse breResponse = BREExecutionResponse.builder().error("some error").build();
        when(breExecutionService.execute(eq("BRE_1"), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(pendingActionRepositoryWrapper, never()).save(any());
    }

    @Test
    void processTaskCompletion_whenBREThrowsException_skipsActions() {
        stubFullTaskCompletionFlow();
        when(breExecutionService.execute(eq("BRE_1"), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("bre error")));

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(pendingActionRepositoryWrapper, never()).save(any());
    }

    @Test
    void processTaskCompletion_whenBREResponseMapNull_skipsActions() {
        stubFullTaskCompletionFlow();
        BREExecutionResponse breResponse = BREExecutionResponse.builder().response(null).build();
        when(breExecutionService.execute(eq("BRE_1"), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(pendingActionRepositoryWrapper, never()).save(any());
    }

    @Test
    void processTaskCompletion_whenActionsKeyMissing_skipsActions() {
        stubFullTaskCompletionFlow();
        BREExecutionResponse breResponse = BREExecutionResponse.builder().response(Map.of("other", "data")).build();
        when(breExecutionService.execute(eq("BRE_1"), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(pendingActionRepositoryWrapper, never()).save(any());
    }

    @Test
    void processTaskCompletion_whenActionConversionFails_skipsActions() {
        stubFullTaskCompletionFlow();
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("actions", "bad_data");
        BREExecutionResponse breResponse = BREExecutionResponse.builder().response(responseMap).build();
        when(breExecutionService.execute(eq("BRE_1"), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));
        when(objectMapper.convertValue(eq("bad_data"), any(TypeReference.class)))
                .thenThrow(new IllegalArgumentException("bad conversion"));

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(pendingActionRepositoryWrapper, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  processTaskCompletion: action processing
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void processTaskCompletion_whenAutoExecuteFalse_storesPendingAction() {
        stubFullTaskCompletionFlow();
        PostTaskAction action = PostTaskAction.builder()
                .type(PostTaskAction.TYPE_MOVE_STAGE).targetStageKey("NEXT").autoExecute(false).build();
        stubBREResponse(action);

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(pendingActionRepositoryWrapper).save(any(PendingWorkflowAction.class));
    }

    @Test
    void processTaskCompletion_autoExecuteMoveStage_callsTransitionStage() {
        EntityWorkflowAdapter adapter = stubFullTaskCompletionFlow();
        PostTaskAction action = PostTaskAction.builder()
                .type(PostTaskAction.TYPE_MOVE_STAGE).targetStageKey("NEXT").assignTo("user2").autoExecute(true).build();
        stubBREResponse(action);

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(adapter).transitionStage(entityIdentifier, "STAGE", "NEXT", "user2");
    }

    @Test
    void processTaskCompletion_autoExecuteCreateTask_callsCreateTaskAndAssociate() {
        EntityWorkflowAdapter adapter = stubFullTaskCompletionFlow();
        when(adapter.getEntity(1L)).thenReturn(new Object());
        when(adapter.getEntityIdentifier(any())).thenReturn(entityIdentifier);
        when(adapter.getPreferredCallWindow(any())).thenReturn(null);

        PostTaskAction action = PostTaskAction.builder()
                .type(PostTaskAction.TYPE_CREATE_TASK).taskConfigKey("NEW_TASK").assignTo("user2").autoExecute(true).build();
        stubBREResponse(action);

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(adapter).createTaskAndAssociate(eq(1L), any(CreateTaskRequest.class), anyMap());
    }

    @Test
    void processTaskCompletion_autoExecuteCloseTasks_withCustomOutcome_usesCustomOutcome() {
        EntityWorkflowAdapter adapter = stubFullTaskCompletionFlow();
        PostTaskAction action = PostTaskAction.builder()
                .type(PostTaskAction.TYPE_CLOSE_TASKS).outcome("REJECTED").autoExecute(true).build();
        stubBREResponse(action);

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(adapter).closeOpenTasks(entityIdentifier, "REJECTED");
    }

    @Test
    void processTaskCompletion_autoExecuteCloseTasks_withNullOutcome_usesAutoClosedDefault() {
        EntityWorkflowAdapter adapter = stubFullTaskCompletionFlow();
        PostTaskAction action = PostTaskAction.builder()
                .type(PostTaskAction.TYPE_CLOSE_TASKS).outcome(null).autoExecute(true).build();
        stubBREResponse(action);

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(adapter).closeOpenTasks(entityIdentifier, "AUTO_CLOSED");
    }

    @Test
    void processTaskCompletion_autoExecuteChangeSubstage_callsChangeSubStage() {
        EntityWorkflowAdapter adapter = stubFullTaskCompletionFlow();
        PostTaskAction action = PostTaskAction.builder()
                .type(PostTaskAction.TYPE_CHANGE_SUBSTAGE).targetSubStageKey("SUB_2").autoExecute(true).build();
        stubBREResponse(action);

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(adapter).changeSubStage(entityIdentifier, "STAGE", "SUB_2");
    }

    @Test
    void processTaskCompletion_autoExecuteUnknownType_doesNotThrow() {
        stubFullTaskCompletionFlow();
        PostTaskAction action = PostTaskAction.builder().type("UNKNOWN").autoExecute(true).build();
        stubBREResponse(action);

        assertDoesNotThrow(() -> service.processTaskCompletion(
                entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user"),
                "Unknown action type should be silently skipped");
    }

    @Test
    void processTaskCompletion_autoExecuteFailure_continuesWithRemainingActions() {
        EntityWorkflowAdapter adapter = stubFullTaskCompletionFlow();
        PostTaskAction failingAction = PostTaskAction.builder()
                .type(PostTaskAction.TYPE_MOVE_STAGE).targetStageKey("NEXT").autoExecute(true).build();
        PostTaskAction pendingAction = PostTaskAction.builder()
                .type(PostTaskAction.TYPE_CREATE_TASK).taskConfigKey("T2").autoExecute(false).build();
        stubBREResponse(failingAction, pendingAction);
        doThrow(new RuntimeException("transition failed")).when(adapter).transitionStage(any(), any(), any(), any());

        service.processTaskCompletion(entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user");

        verify(pendingActionRepositoryWrapper).save(any(PendingWorkflowAction.class));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  executePendingAction
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void executePendingAction_whenAlreadyProcessed_throwsValidationException() {
        UUID actionId = UUID.randomUUID();
        PendingWorkflowAction pending = new PendingWorkflowAction();
        pending.setStatus(PendingWorkflowAction.STATUS_EXECUTED);
        when(pendingActionRepositoryWrapper.findByActionIdentifierWithException(actionId)).thenReturn(pending);

        assertThrows(WorkflowValidationException.class,
                () -> service.executePendingAction(actionId, "user", LocalDateTime.now()),
                "Already processed action should throw WorkflowValidationException");
    }

    @Test
    void executePendingAction_moveStageAction_executesAndMarksAsExecuted() {
        UserContext.setUsername("test_user");
        UUID actionId = UUID.randomUUID();
        PendingWorkflowAction pending = buildPendingAction(PostTaskAction.TYPE_MOVE_STAGE, "NEXT_STAGE", null);
        when(pendingActionRepositoryWrapper.findByActionIdentifierWithException(actionId)).thenReturn(pending);
        EntityWorkflowAdapter adapter = mock(EntityWorkflowAdapter.class);
        when(adapterRegistry.getAdapter(EntityType.LEAD)).thenReturn(adapter);
        when(adapter.resolveEntityId(pending.getEntityIdentifier())).thenReturn(1L);

        LocalDateTime dueDate = LocalDateTime.of(2026, 6, 1, 10, 0);
        service.executePendingAction(actionId, "assigned_user", dueDate);

        verify(adapter).transitionStage(pending.getEntityIdentifier(), "STAGE", "NEXT_STAGE", "assigned_user");
        assertEquals(PendingWorkflowAction.STATUS_EXECUTED, pending.getStatus(),
                "Pending action status should be EXECUTED after processing");
        verify(pendingActionRepositoryWrapper).save(pending);
    }

    @Test
    void executePendingAction_createTaskAction_withNullEntityInfo_setsNullEntityId() {
        UserContext.setUsername("test_user");
        UUID actionId = UUID.randomUUID();
        PendingWorkflowAction pending = buildPendingAction(PostTaskAction.TYPE_CREATE_TASK, null, "NEW_TASK");
        when(pendingActionRepositoryWrapper.findByActionIdentifierWithException(actionId)).thenReturn(pending);
        EntityWorkflowAdapter adapter = mock(EntityWorkflowAdapter.class);
        when(adapterRegistry.getAdapter(EntityType.LEAD)).thenReturn(adapter);
        when(adapter.resolveEntityId(pending.getEntityIdentifier())).thenReturn(1L);
        when(adapter.getEntity(1L)).thenReturn(null);

        service.executePendingAction(actionId, "assigned_user", null);

        ArgumentCaptor<CreateTaskRequest> captor = ArgumentCaptor.forClass(CreateTaskRequest.class);
        verify(adapter).createTaskAndAssociate(eq(1L), captor.capture(), anyMap());
        assertNull(captor.getValue().getTaskDetails().getEntityId(),
                "Entity ID in task details should be null when entity info is null");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  cancelPendingAction
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void cancelPendingAction_whenAlreadyProcessed_throwsValidationException() {
        UUID actionId = UUID.randomUUID();
        PendingWorkflowAction pending = new PendingWorkflowAction();
        pending.setStatus(PendingWorkflowAction.STATUS_CANCELLED);
        when(pendingActionRepositoryWrapper.findByActionIdentifierWithException(actionId)).thenReturn(pending);

        assertThrows(WorkflowValidationException.class, () -> service.cancelPendingAction(actionId),
                "Already cancelled action should throw WorkflowValidationException");
    }

    @Test
    void cancelPendingAction_withPendingStatus_cancelsAndSaves() {
        UserContext.setUsername("test_user");
        UUID actionId = UUID.randomUUID();
        PendingWorkflowAction pending = new PendingWorkflowAction();
        pending.setStatus(PendingWorkflowAction.STATUS_PENDING);
        when(pendingActionRepositoryWrapper.findByActionIdentifierWithException(actionId)).thenReturn(pending);

        service.cancelPendingAction(actionId);

        assertEquals(PendingWorkflowAction.STATUS_CANCELLED, pending.getStatus(),
                "Action status should be CANCELLED after cancellation");
        assertNotNull(pending.getExecutedAt(), "ExecutedAt timestamp should be set on cancellation");
        verify(pendingActionRepositoryWrapper).save(pending);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  cancelPendingActionsBySourceTask
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void cancelPendingActionsBySourceTask_withMultipleActions_cancelsAll() {
        UserContext.setUsername("test_user");
        PendingWorkflowAction p1 = new PendingWorkflowAction();
        p1.setStatus(PendingWorkflowAction.STATUS_PENDING);
        PendingWorkflowAction p2 = new PendingWorkflowAction();
        p2.setStatus(PendingWorkflowAction.STATUS_PENDING);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(taskIdentifier))
                .thenReturn(List.of(p1, p2));

        service.cancelPendingActionsBySourceTask(taskIdentifier);

        assertEquals(PendingWorkflowAction.STATUS_CANCELLED, p1.getStatus(),
                "First pending action should be cancelled");
        assertEquals(PendingWorkflowAction.STATUS_CANCELLED, p2.getStatus(),
                "Second pending action should be cancelled");
        verify(pendingActionRepositoryWrapper, times(2)).save(any(PendingWorkflowAction.class));
    }

    @Test
    void cancelPendingActionsBySourceTask_withNoActions_doesNotSave() {
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(taskIdentifier))
                .thenReturn(Collections.emptyList());

        service.cancelPendingActionsBySourceTask(taskIdentifier);

        verify(pendingActionRepositoryWrapper, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════

    private WorkflowConfig buildWorkflowConfigEntity() {
        WorkflowConfig config = new WorkflowConfig();
        config.setWorkflowConfigDetails(WorkflowConfig.WorkflowConfigDetails.builder().stages(List.of()).build());
        return config;
    }

    private StageConfig buildStageConfigWithDetails() {
        StageConfig stageConfig = new StageConfig();
        stageConfig.setKey("STG");
        stageConfig.setName("Stage Name");
        stageConfig.setDescription("desc");
        stageConfig.setStageConfig(StageConfig.StageConfigDetails.builder()
                .possibleNextStages(List.of(StageConfig.PossibleNextStage.builder()
                        .stageKey("NEXT").allowedRoles(List.of("ROLE_A")).build()))
                .build());
        stageConfig.setAssigneeRoles(StageConfig.AssigneeRoles.builder().roles(List.of("ROLE_B")).build());
        stageConfig.setSubStagesCode("SUB_CODE");
        stageConfig.setIsActive(true);
        return stageConfig;
    }

    private void stubWorkflowWithOneTask() {
        WorkflowConfig wfConfig = buildWorkflowConfigEntity();
        StageTaskConfig stageTaskConfig = StageTaskConfig.builder()
                .fromStage("DEFAULT").taskConfigKeys(List.of("TASK_1")).build();
        WorkflowStageConfig stageConfig = WorkflowStageConfig.builder()
                .stageKey("TO").stageTasks(List.of(stageTaskConfig)).build();
        WorkflowConfigDto wfDto = WorkflowConfigDto.builder().stages(List.of(stageConfig)).build();

        TaskConfig taskConfigEntity = mock(TaskConfig.class);
        when(taskConfigEntity.getTaskConfigKey()).thenReturn("TASK_1");
        when(taskConfigEntity.getTaskConfigDetails()).thenReturn(null);

        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(wfConfig);
        when(objectMapper.convertValue(any(), eq(WorkflowConfigDto.class))).thenReturn(wfDto);
        when(taskConfigRepositoryWrapper.findActiveByTaskConfigKeys(List.of("TASK_1")))
                .thenReturn(Map.of("TASK_1", taskConfigEntity));
    }

    private EntityWorkflowAdapter stubAdapterWithEntity() {
        EntityWorkflowAdapter adapter = mock(EntityWorkflowAdapter.class);
        when(adapterRegistry.getAdapter(EntityType.LEAD)).thenReturn(adapter);
        when(adapter.getEntity(1L)).thenReturn(new Object());
        when(adapter.getEntityIdentifier(any())).thenReturn(UUID.randomUUID());
        when(adapter.getPreferredCallWindow(any())).thenReturn(null);
        return adapter;
    }

    private EntityWorkflowAdapter stubAdapterForTaskCompletion(String workflowConfigKey) {
        EntityWorkflowAdapter adapter = mock(EntityWorkflowAdapter.class);
        when(adapterRegistry.getAdapter(EntityType.LEAD)).thenReturn(adapter);
        when(adapter.resolveEntityId(entityIdentifier)).thenReturn(1L);
        when(adapter.getWorkflowConfigKey(1L)).thenReturn(workflowConfigKey);
        return adapter;
    }

    private void stubParsedWorkflowWithStages(List<WorkflowStageConfig> stages) {
        WorkflowConfig wfConfig = buildWorkflowConfigEntity();
        WorkflowConfigDto wfDto = WorkflowConfigDto.builder().stages(stages).build();
        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(wfConfig);
        when(objectMapper.convertValue(any(), eq(WorkflowConfigDto.class))).thenReturn(wfDto);
    }

    private EntityWorkflowAdapter stubFullTaskCompletionFlow() {
        EntityWorkflowAdapter adapter = stubAdapterForTaskCompletion("WF");
        TaskCompletionRule rule = TaskCompletionRule.builder().taskConfigKey("TASK").breRuleUname("BRE_1").build();
        stubParsedWorkflowWithStages(List.of(
                WorkflowStageConfig.builder().stageKey("STAGE").taskCompletionRules(List.of(rule)).build()));
        return adapter;
    }

    private void stubBREResponse(PostTaskAction... actions) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("actions", List.of(actions));
        BREExecutionResponse breResponse = BREExecutionResponse.builder().response(responseMap).build();
        when(breExecutionService.execute(eq("BRE_1"), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));
        when(objectMapper.convertValue(any(), any(TypeReference.class))).thenReturn(List.of(actions));
    }

    private void stubWorkflowWithStage(String stageKey, String defaultSubStage) {
        WorkflowConfig config = new WorkflowConfig();
        config.setWorkflowConfigDetails(WorkflowConfig.WorkflowConfigDetails.builder()
                .stages(List.of(WorkflowStageConfig.builder()
                        .stageKey(stageKey).defaultSubStage(defaultSubStage).build()))
                .build());
        when(workflowConfigReadService.getWorkflowConfigByKey("WF")).thenReturn(config);
    }

    private PendingWorkflowAction buildPendingAction(String actionType, String targetStageKey, String taskConfigKey) {
        PendingWorkflowAction pending = new PendingWorkflowAction();
        pending.setStatus(PendingWorkflowAction.STATUS_PENDING);
        pending.setEntityIdentifier(entityIdentifier);
        pending.setEntityType(EntityType.LEAD);
        pending.setCurrentStageKey("STAGE");
        pending.setActionDetails(PendingWorkflowAction.ActionDetails.builder()
                .type(actionType)
                .targetStageKey(targetStageKey)
                .taskConfigKey(taskConfigKey)
                .build());
        return pending;
    }
}
