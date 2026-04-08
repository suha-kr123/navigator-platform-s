package com.nivasafinance.features.workflow.service.impl;

import com.nivasafinance.features.workflow.dto.WorkflowStageConfig;
import com.nivasafinance.features.workflow.entity.WorkflowConfig;
import com.nivasafinance.features.workflow.exception.WorkflowValidationException;
import com.nivasafinance.features.workflow.repository.WorkflowConfigRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowConfigReadServiceImplTest {

    @Mock
    private WorkflowConfigRepositoryWrapper workflowConfigRepositoryWrapper;

    @InjectMocks
    private WorkflowConfigReadServiceImpl service;

    // ── getWorkflowConfigByKey ──

    @Test
    void getWorkflowConfigByKey_whenConfigExists_returnsConfig() {
        WorkflowConfig config = new WorkflowConfig();
        config.setWorkflowConfigKey("WF_KEY");
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(config);

        WorkflowConfig result = service.getWorkflowConfigByKey("WF_KEY");

        assertEquals("WF_KEY", result.getWorkflowConfigKey(),
                "Returned config key should match the requested key");
        verify(workflowConfigRepositoryWrapper).findActiveByWorkflowConfigKey("WF_KEY");
    }

    // ── canCreateAdhocTask: validation ──

    @ParameterizedTest
    @NullAndEmptySource
    void canCreateAdhocTask_withInvalidWorkflowConfigKey_throwsValidationException(String workflowConfigKey) {
        assertThrows(WorkflowValidationException.class,
                () -> service.canCreateAdhocTask(workflowConfigKey, "TASK_KEY", "STAGE_KEY"),
                "Null or empty workflowConfigKey should throw WorkflowValidationException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void canCreateAdhocTask_withInvalidTaskConfigKey_throwsValidationException(String taskConfigKey) {
        assertThrows(WorkflowValidationException.class,
                () -> service.canCreateAdhocTask("WF_KEY", taskConfigKey, "STAGE_KEY"),
                "Null or empty taskConfigKey should throw WorkflowValidationException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void canCreateAdhocTask_withInvalidStageKey_throwsValidationException(String stageKey) {
        assertThrows(WorkflowValidationException.class,
                () -> service.canCreateAdhocTask("WF_KEY", "TASK_KEY", stageKey),
                "Null or empty stageKey should throw WorkflowValidationException");
    }

    // ── canCreateAdhocTask: business logic ──

    @Test
    void canCreateAdhocTask_whenWorkflowConfigIsNull_returnsFalse() {
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(null);

        boolean result = service.canCreateAdhocTask("WF_KEY", "TASK_KEY", "STAGE_KEY");

        assertFalse(result, "Should return false when workflow config is not found");
    }

    @Test
    void canCreateAdhocTask_whenStageNotFound_returnsFalse() {
        WorkflowConfig config = buildWorkflowConfig("OTHER_STAGE", List.of("TASK_KEY"));
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(config);

        boolean result = service.canCreateAdhocTask("WF_KEY", "TASK_KEY", "STAGE_KEY");

        assertFalse(result, "Should return false when the requested stage is not in the workflow config");
    }

    @Test
    void canCreateAdhocTask_whenTaskNotInAllowedList_returnsFalse() {
        WorkflowConfig config = buildWorkflowConfig("STAGE_KEY", List.of("OTHER_TASK"));
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(config);

        boolean result = service.canCreateAdhocTask("WF_KEY", "TASK_KEY", "STAGE_KEY");

        assertFalse(result, "Should return false when task is not in the stage's allowed adhoc tasks");
    }

    @Test
    void canCreateAdhocTask_whenTaskIsInAllowedList_returnsTrue() {
        WorkflowConfig config = buildWorkflowConfig("STAGE_KEY", List.of("TASK_KEY", "OTHER_TASK"));
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(config);

        boolean result = service.canCreateAdhocTask("WF_KEY", "TASK_KEY", "STAGE_KEY");

        assertTrue(result, "Should return true when task is in the stage's allowed adhoc tasks");
    }

    // ── getAdhocTaskKeysForStage: validation ──

    @ParameterizedTest
    @NullAndEmptySource
    void getAdhocTaskKeysForStage_withInvalidWorkflowConfigKey_throwsValidationException(String workflowConfigKey) {
        assertThrows(WorkflowValidationException.class,
                () -> service.getAdhocTaskKeysForStage(workflowConfigKey, "STAGE_KEY"),
                "Null or empty workflowConfigKey should throw WorkflowValidationException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getAdhocTaskKeysForStage_withInvalidStageKey_throwsValidationException(String stageKey) {
        assertThrows(WorkflowValidationException.class,
                () -> service.getAdhocTaskKeysForStage("WF_KEY", stageKey),
                "Null or empty stageKey should throw WorkflowValidationException");
    }

    // ── getAdhocTaskKeysForStage: null guards ──

    @Test
    void getAdhocTaskKeysForStage_whenWorkflowConfigIsNull_returnsEmptyList() {
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(null);

        List<String> result = service.getAdhocTaskKeysForStage("WF_KEY", "STAGE_KEY");

        assertTrue(result.isEmpty(), "Should return empty list when workflow config is null");
    }

    @Test
    void getAdhocTaskKeysForStage_whenConfigDetailsIsNull_returnsEmptyList() {
        WorkflowConfig config = new WorkflowConfig();
        config.setWorkflowConfigDetails(null);
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(config);

        List<String> result = service.getAdhocTaskKeysForStage("WF_KEY", "STAGE_KEY");

        assertTrue(result.isEmpty(), "Should return empty list when workflow config details are null");
    }

    @Test
    void getAdhocTaskKeysForStage_whenStagesIsNull_returnsEmptyList() {
        WorkflowConfig config = new WorkflowConfig();
        config.setWorkflowConfigDetails(WorkflowConfig.WorkflowConfigDetails.builder().stages(null).build());
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(config);

        List<String> result = service.getAdhocTaskKeysForStage("WF_KEY", "STAGE_KEY");

        assertTrue(result.isEmpty(), "Should return empty list when stages list is null");
    }

    @Test
    void getAdhocTaskKeysForStage_whenStageNotFound_returnsEmptyList() {
        WorkflowConfig config = buildWorkflowConfig("OTHER_STAGE", List.of("TASK_KEY"));
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(config);

        List<String> result = service.getAdhocTaskKeysForStage("WF_KEY", "STAGE_KEY");

        assertTrue(result.isEmpty(), "Should return empty list when the requested stage is not found");
    }

    @Test
    void getAdhocTaskKeysForStage_whenAllowedAdhocTasksIsNull_returnsEmptyList() {
        WorkflowConfig config = buildWorkflowConfig("STAGE_KEY", null);
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(config);

        List<String> result = service.getAdhocTaskKeysForStage("WF_KEY", "STAGE_KEY");

        assertTrue(result.isEmpty(), "Should return empty list when allowed adhoc tasks list is null");
    }

    @Test
    void getAdhocTaskKeysForStage_whenAllowedAdhocTasksIsEmpty_returnsEmptyList() {
        WorkflowConfig config = buildWorkflowConfig("STAGE_KEY", new ArrayList<>());
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(config);

        List<String> result = service.getAdhocTaskKeysForStage("WF_KEY", "STAGE_KEY");

        assertTrue(result.isEmpty(), "Should return empty list when allowed adhoc tasks list is empty");
    }

    // ── getAdhocTaskKeysForStage: happy path ──

    @Test
    void getAdhocTaskKeysForStage_whenTaskKeysExist_returnsTaskKeys() {
        List<String> expectedTasks = List.of("TASK_A", "TASK_B");
        WorkflowConfig config = buildWorkflowConfig("STAGE_KEY", expectedTasks);
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey("WF_KEY")).thenReturn(config);

        List<String> result = service.getAdhocTaskKeysForStage("WF_KEY", "STAGE_KEY");

        assertEquals(expectedTasks, result, "Should return the adhoc task keys configured for the stage");
    }

    // ── helper ──

    private WorkflowConfig buildWorkflowConfig(String stageKey, List<String> allowedAdhocTasks) {
        WorkflowStageConfig stageConfig = WorkflowStageConfig.builder()
                .stageKey(stageKey)
                .allowedAdhocTasks(allowedAdhocTasks)
                .build();

        WorkflowConfig config = new WorkflowConfig();
        config.setWorkflowConfigKey("WF_KEY");
        config.setWorkflowConfigDetails(WorkflowConfig.WorkflowConfigDetails.builder()
                .stages(List.of(stageConfig))
                .build());
        return config;
    }
}
