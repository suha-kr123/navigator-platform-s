package com.nivasafinance.features.workflow.service.impl;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.repository.StageConfigRepositoryWrapper;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.workflow.dto.PendingActionResponse;
import com.nivasafinance.features.workflow.dto.PostTaskAction;
import com.nivasafinance.features.workflow.entity.PendingWorkflowAction;
import com.nivasafinance.features.workflow.repository.PendingWorkflowActionRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingWorkflowActionServiceImplTest {

    @Mock
    private PendingWorkflowActionRepositoryWrapper pendingActionRepositoryWrapper;

    @Mock
    private TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;

    @Mock
    private StageConfigRepositoryWrapper stageConfigRepositoryWrapper;

    @InjectMocks
    private PendingWorkflowActionServiceImpl service;

    private UUID sourceTaskId;
    private UUID entityId;

    @BeforeEach
    void setUp() {
        sourceTaskId = UUID.randomUUID();
        entityId = UUID.randomUUID();
    }

    // ── getPendingActionsBySourceTask ──

    @Test
    void getPendingActionsBySourceTask_whenNoActions_returnsEmptyList() {
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(sourceTaskId))
                .thenReturn(Collections.emptyList());

        List<PendingActionResponse> result = service.getPendingActionsBySourceTask(sourceTaskId);

        assertTrue(result.isEmpty(), "Should return empty list when no pending actions exist for source task");
        verify(pendingActionRepositoryWrapper).findPendingBySourceTask(sourceTaskId);
    }

    @Test
    void getPendingActionsBySourceTask_whenActionsExist_returnsMappedResponses() {
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_CLOSE_TASKS, null, null, null);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(sourceTaskId))
                .thenReturn(List.of(action));

        List<PendingActionResponse> result = service.getPendingActionsBySourceTask(sourceTaskId);

        assertEquals(1, result.size(), "Should return one mapped response for one pending action");
        assertEquals(action.getActionIdentifier(), result.get(0).getActionIdentifier(),
                "Response action identifier should match the entity action identifier");
    }

    // ── getPendingActionsByEntity ──

    @Test
    void getPendingActionsByEntity_whenNoActions_returnsEmptyList() {
        when(pendingActionRepositoryWrapper.findPendingByEntity(entityId, EntityType.LEAD))
                .thenReturn(Collections.emptyList());

        List<PendingActionResponse> result = service.getPendingActionsByEntity(entityId, EntityType.LEAD);

        assertTrue(result.isEmpty(), "Should return empty list when no pending actions exist for entity");
        verify(pendingActionRepositoryWrapper).findPendingByEntity(entityId, EntityType.LEAD);
    }

    @Test
    void getPendingActionsByEntity_whenActionsExist_returnsMappedResponses() {
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_CLOSE_TASKS, null, null, null);
        when(pendingActionRepositoryWrapper.findPendingByEntity(entityId, EntityType.LEAD))
                .thenReturn(List.of(action));

        List<PendingActionResponse> result = service.getPendingActionsByEntity(entityId, EntityType.LEAD);

        assertEquals(1, result.size(), "Should return one mapped response for one pending action");
        assertEquals(action.getEntityIdentifier(), result.get(0).getEntityIdentifier(),
                "Response entity identifier should match the entity's identifier");
    }

    // ── toResponse: CREATE_TASK ──

    @Test
    void toResponse_createTask_withAllowedRoles_enrichesTaskMetadata() {
        String taskConfigKey = "TASK_KEY";
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_CREATE_TASK, taskConfigKey, null, null);

        TaskConfig taskConfig = mock(TaskConfig.class);
        TaskConfig.TaskConfigDetails details = mock(TaskConfig.TaskConfigDetails.class);
        when(taskConfig.getName()).thenReturn("My Task");
        when(taskConfig.getTaskConfigDetails()).thenReturn(details);
        when(details.getAllowedRoles()).thenReturn(List.of("ROLE_A", "ROLE_B"));
        when(taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey)).thenReturn(taskConfig);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertEquals("My Task", response.getTaskName(), "Task name should come from task config");
        assertEquals(List.of("ROLE_A", "ROLE_B"), response.getFields().getAssignTo().getAllowedRoles(),
                "Allowed roles should be populated from task config details");
        assertTrue(response.getFields().getAssignTo().isRequired(),
                "AssignTo should be required for CREATE_TASK action");
        assertTrue(response.getFields().getDueDate().isRequired(),
                "DueDate should be required for CREATE_TASK action");
    }

    @Test
    void toResponse_createTask_withNullTaskConfigDetails_usesEmptyRoles() {
        String taskConfigKey = "TASK_KEY";
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_CREATE_TASK, taskConfigKey, null, null);

        TaskConfig taskConfig = mock(TaskConfig.class);
        when(taskConfig.getName()).thenReturn("My Task");
        when(taskConfig.getTaskConfigDetails()).thenReturn(null);
        when(taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey)).thenReturn(taskConfig);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertTrue(response.getFields().getAssignTo().getAllowedRoles().isEmpty(),
                "Allowed roles should be empty when task config details are null");
    }

    @Test
    void toResponse_createTask_withNullAllowedRoles_usesEmptyRoles() {
        String taskConfigKey = "TASK_KEY";
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_CREATE_TASK, taskConfigKey, null, null);

        TaskConfig taskConfig = mock(TaskConfig.class);
        TaskConfig.TaskConfigDetails details = mock(TaskConfig.TaskConfigDetails.class);
        when(taskConfig.getName()).thenReturn("My Task");
        when(taskConfig.getTaskConfigDetails()).thenReturn(details);
        when(details.getAllowedRoles()).thenReturn(null);
        when(taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey)).thenReturn(taskConfig);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertTrue(response.getFields().getAssignTo().getAllowedRoles().isEmpty(),
                "Allowed roles should be empty when allowedRoles is null in task config details");
    }

    @Test
    void toResponse_createTask_whenTaskConfigNotFound_fallsBackToConfigKey() {
        String taskConfigKey = "TASK_KEY";
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_CREATE_TASK, taskConfigKey, null, null);

        when(taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey))
                .thenThrow(new RuntimeException("not found"));
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertEquals(taskConfigKey, response.getTaskName(),
                "Task name should fall back to taskConfigKey when config lookup fails");
    }

    // ── toResponse: MOVE_STAGE ──

    @Test
    void toResponse_moveStage_withAssigneeRoles_enrichesStageMetadata() {
        String targetStageKey = "STAGE_KEY";
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_MOVE_STAGE, null, targetStageKey, null);

        StageConfig stageConfig = mock(StageConfig.class);
        StageConfig.AssigneeRoles assigneeRoles = mock(StageConfig.AssigneeRoles.class);
        when(stageConfig.getName()).thenReturn("Approval Stage");
        when(stageConfig.getAssigneeRoles()).thenReturn(assigneeRoles);
        when(assigneeRoles.getRoles()).thenReturn(List.of("MANAGER"));
        when(stageConfigRepositoryWrapper.findByKeyWithException(targetStageKey)).thenReturn(stageConfig);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertEquals("Approval Stage", response.getTargetStageName(),
                "Target stage name should come from stage config");
        assertEquals(List.of("MANAGER"), response.getFields().getAssignTo().getAllowedRoles(),
                "Allowed roles should be populated from stage config assignee roles");
        assertFalse(response.getFields().getDueDate().isRequired(),
                "DueDate should not be required for MOVE_STAGE action");
    }

    @Test
    void toResponse_moveStage_withNullAssigneeRoles_usesEmptyRoles() {
        String targetStageKey = "STAGE_KEY";
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_MOVE_STAGE, null, targetStageKey, null);

        StageConfig stageConfig = mock(StageConfig.class);
        when(stageConfig.getName()).thenReturn("Approval Stage");
        when(stageConfig.getAssigneeRoles()).thenReturn(null);
        when(stageConfigRepositoryWrapper.findByKeyWithException(targetStageKey)).thenReturn(stageConfig);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertTrue(response.getFields().getAssignTo().getAllowedRoles().isEmpty(),
                "Allowed roles should be empty when assigneeRoles is null");
    }

    @Test
    void toResponse_moveStage_withNullRolesInAssigneeRoles_usesEmptyRoles() {
        String targetStageKey = "STAGE_KEY";
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_MOVE_STAGE, null, targetStageKey, null);

        StageConfig stageConfig = mock(StageConfig.class);
        StageConfig.AssigneeRoles assigneeRoles = mock(StageConfig.AssigneeRoles.class);
        when(stageConfig.getName()).thenReturn("Approval Stage");
        when(stageConfig.getAssigneeRoles()).thenReturn(assigneeRoles);
        when(assigneeRoles.getRoles()).thenReturn(null);
        when(stageConfigRepositoryWrapper.findByKeyWithException(targetStageKey)).thenReturn(stageConfig);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertTrue(response.getFields().getAssignTo().getAllowedRoles().isEmpty(),
                "Allowed roles should be empty when roles list is null inside assigneeRoles");
    }

    @Test
    void toResponse_moveStage_whenStageConfigNotFound_fallsBackToStageKey() {
        String targetStageKey = "STAGE_KEY";
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_MOVE_STAGE, null, targetStageKey, null);

        when(stageConfigRepositoryWrapper.findByKeyWithException(targetStageKey))
                .thenThrow(new RuntimeException("not found"));
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertEquals(targetStageKey, response.getTargetStageName(),
                "Target stage name should fall back to stageKey when config lookup fails");
    }

    // ── toResponse: CLOSE_TASKS ──

    @Test
    void toResponse_closeTasks_setsOptionalFields() {
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_CLOSE_TASKS, null, null, null);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertEquals(PostTaskAction.TYPE_CLOSE_TASKS, response.getActionType(),
                "Action type should be CLOSE_TASKS");
        assertFalse(response.getFields().getAssignTo().isRequired(),
                "AssignTo should not be required for CLOSE_TASKS action");
        assertFalse(response.getFields().getDueDate().isRequired(),
                "DueDate should not be required for CLOSE_TASKS action");
    }

    // ── toResponse: CHANGE_SUBSTAGE ──

    @Test
    void toResponse_changeSubstage_setsTargetSubStageKeyAndOptionalFields() {
        String targetSubStageKey = "SUB_STAGE_1";
        PendingWorkflowAction action = buildAction(PostTaskAction.TYPE_CHANGE_SUBSTAGE, null, null, targetSubStageKey);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertEquals(targetSubStageKey, response.getTargetSubStageKey(),
                "Target substage key should be set from action details");
        assertFalse(response.getFields().getAssignTo().isRequired(),
                "AssignTo should not be required for CHANGE_SUBSTAGE action");
    }

    // ── toResponse: unknown action type ──

    @Test
    void toResponse_unknownActionType_doesNotSetFields() {
        PendingWorkflowAction action = buildAction("UNKNOWN_TYPE", null, null, null);
        when(pendingActionRepositoryWrapper.findPendingBySourceTask(action.getSourceTaskIdentifier()))
                .thenReturn(List.of(action));

        PendingActionResponse response = service.getPendingActionsBySourceTask(action.getSourceTaskIdentifier()).get(0);

        assertEquals("UNKNOWN_TYPE", response.getActionType(),
                "Action type should be preserved even when unknown");
        assertNull(response.getFields(),
                "Fields should be null for unknown action types");
    }

    // ── helper ──

    private PendingWorkflowAction buildAction(String actionType, String taskConfigKey,
            String targetStageKey, String targetSubStageKey) {
        PendingWorkflowAction action = new PendingWorkflowAction();
        action.setEntityIdentifier(entityId);
        action.setEntityType(EntityType.LEAD);
        action.setCurrentStageKey("CURRENT_STAGE");
        action.setSourceTaskIdentifier(sourceTaskId);
        action.setSourceTaskConfigKey("SRC_TASK_CONFIG");
        action.setSourceOutcome("APPROVED");
        action.setStatus(PendingWorkflowAction.STATUS_PENDING);
        action.setCreatedAt(LocalDateTime.now());
        action.setActionDetails(PendingWorkflowAction.ActionDetails.builder()
                .type(actionType)
                .taskConfigKey(taskConfigKey)
                .targetStageKey(targetStageKey)
                .targetSubStageKey(targetSubStageKey)
                .build());
        ReflectionTestUtils.setField(action, "actionIdentifier", UUID.randomUUID());
        return action;
    }
}
