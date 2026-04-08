package com.nivasafinance.features.workflow.listener;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.StageTransitionEventPayload;
import com.nivasafinance.common.events.payload.TaskCompletedEventPayload;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowEventListenerTest {

    @Mock
    private WorkflowOrchestratorService workflowOrchestratorService;

    @InjectMocks
    private WorkflowEventListener listener;

    private UUID entityIdentifier;
    private UUID taskIdentifier;

    @BeforeEach
    void setUp() {
        entityIdentifier = UUID.randomUUID();
        taskIdentifier = UUID.randomUUID();
    }

    // ── handleSystemEvent (STAGE_TRANSITIONED) ──

    @Test
    void handleSystemEvent_withNonStageTransitionPayload_skipsProcessing() {
        SystemEvent<String> event = new SystemEvent<>("STAGE_TRANSITIONED", "not a payload");

        listener.handleSystemEvent(event);

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleSystemEvent_withNullEntityType_skipsProcessing() {
        StageTransitionEventPayload payload = StageTransitionEventPayload.builder()
                .entityType(null).entityId(1L).toStageKey("TO").build();

        listener.handleSystemEvent(new SystemEvent<>("STAGE_TRANSITIONED", payload));

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleSystemEvent_withNullEntityId_skipsProcessing() {
        StageTransitionEventPayload payload = StageTransitionEventPayload.builder()
                .entityType(EntityType.LEAD).entityId(null).toStageKey("TO").build();

        listener.handleSystemEvent(new SystemEvent<>("STAGE_TRANSITIONED", payload));

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleSystemEvent_withNullToStageKey_skipsProcessing() {
        StageTransitionEventPayload payload = StageTransitionEventPayload.builder()
                .entityType(EntityType.LEAD).entityId(1L).toStageKey(null).build();

        listener.handleSystemEvent(new SystemEvent<>("STAGE_TRANSITIONED", payload));

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleSystemEvent_whenWorkflowConfigKeyNull_skipsTaskCreation() {
        StageTransitionEventPayload payload = StageTransitionEventPayload.builder()
                .entityType(EntityType.LEAD).entityId(1L).fromStageKey("FROM").toStageKey("TO").build();
        when(workflowOrchestratorService.getEntityWorkflowConfigKey(1L, EntityType.LEAD)).thenReturn(null);

        listener.handleSystemEvent(new SystemEvent<>("STAGE_TRANSITIONED", payload));

        verify(workflowOrchestratorService, never()).createTasksForStageTransition(
                anyLong(), any(), any(), any(), any(), any(), anyMap());
    }

    @Test
    void handleSystemEvent_withAssignedTo_includesAssignedToInContext() {
        StageTransitionEventPayload payload = StageTransitionEventPayload.builder()
                .entityType(EntityType.LEAD).entityId(1L).fromStageKey("FROM")
                .toStageKey("TO").assignedTo("user1").build();
        when(workflowOrchestratorService.getEntityWorkflowConfigKey(1L, EntityType.LEAD)).thenReturn("WF_KEY");

        listener.handleSystemEvent(new SystemEvent<>("STAGE_TRANSITIONED", payload));

        verify(workflowOrchestratorService).createTasksForStageTransition(
                eq(1L), eq(EntityType.LEAD), eq("FROM"), eq("TO"), eq("user1"), eq("WF_KEY"),
                argThat(ctx -> "user1".equals(ctx.get("assignedTo"))));
    }

    @Test
    void handleSystemEvent_withoutAssignedTo_excludesAssignedToFromContext() {
        StageTransitionEventPayload payload = StageTransitionEventPayload.builder()
                .entityType(EntityType.LEAD).entityId(1L).fromStageKey("FROM")
                .toStageKey("TO").assignedTo(null).build();
        when(workflowOrchestratorService.getEntityWorkflowConfigKey(1L, EntityType.LEAD)).thenReturn("WF_KEY");

        listener.handleSystemEvent(new SystemEvent<>("STAGE_TRANSITIONED", payload));

        verify(workflowOrchestratorService).createTasksForStageTransition(
                eq(1L), eq(EntityType.LEAD), eq("FROM"), eq("TO"), isNull(), eq("WF_KEY"),
                argThat(ctx -> !ctx.containsKey("assignedTo")));
    }

    @Test
    void handleSystemEvent_whenExceptionThrown_doesNotPropagate() {
        StageTransitionEventPayload payload = StageTransitionEventPayload.builder()
                .entityType(EntityType.LEAD).entityId(1L).fromStageKey("FROM").toStageKey("TO").build();
        when(workflowOrchestratorService.getEntityWorkflowConfigKey(1L, EntityType.LEAD))
                .thenThrow(new RuntimeException("unexpected"));

        listener.handleSystemEvent(new SystemEvent<>("STAGE_TRANSITIONED", payload));

        verify(workflowOrchestratorService, never()).createTasksForStageTransition(
                anyLong(), any(), any(), any(), any(), any(), anyMap());
    }

    // ── handleTaskCompletedEvent (TASK_COMPLETED) ──

    @Test
    void handleTaskCompletedEvent_withNonTaskCompletedPayload_skipsProcessing() {
        SystemEvent<String> event = new SystemEvent<>("TASK_COMPLETED", "not a payload");

        listener.handleTaskCompletedEvent(event);

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleTaskCompletedEvent_withNullEntityIdentifier_skipsProcessing() {
        TaskCompletedEventPayload payload = TaskCompletedEventPayload.builder()
                .entityIdentifier(null).entityType(EntityType.LEAD)
                .taskConfigKey("TASK").stageKey("STAGE").build();

        listener.handleTaskCompletedEvent(new SystemEvent<>("TASK_COMPLETED", payload));

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleTaskCompletedEvent_withNullEntityType_skipsProcessing() {
        TaskCompletedEventPayload payload = TaskCompletedEventPayload.builder()
                .entityIdentifier(entityIdentifier).entityType(null)
                .taskConfigKey("TASK").stageKey("STAGE").build();

        listener.handleTaskCompletedEvent(new SystemEvent<>("TASK_COMPLETED", payload));

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleTaskCompletedEvent_withNullTaskConfigKey_skipsProcessing() {
        TaskCompletedEventPayload payload = TaskCompletedEventPayload.builder()
                .entityIdentifier(entityIdentifier).entityType(EntityType.LEAD)
                .taskConfigKey(null).stageKey("STAGE").build();

        listener.handleTaskCompletedEvent(new SystemEvent<>("TASK_COMPLETED", payload));

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleTaskCompletedEvent_withEmptyTaskConfigKey_skipsProcessing() {
        TaskCompletedEventPayload payload = TaskCompletedEventPayload.builder()
                .entityIdentifier(entityIdentifier).entityType(EntityType.LEAD)
                .taskConfigKey("").stageKey("STAGE").build();

        listener.handleTaskCompletedEvent(new SystemEvent<>("TASK_COMPLETED", payload));

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleTaskCompletedEvent_withNullStageKey_skipsProcessing() {
        TaskCompletedEventPayload payload = TaskCompletedEventPayload.builder()
                .entityIdentifier(entityIdentifier).entityType(EntityType.LEAD)
                .taskConfigKey("TASK").stageKey(null).build();

        listener.handleTaskCompletedEvent(new SystemEvent<>("TASK_COMPLETED", payload));

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleTaskCompletedEvent_withEmptyStageKey_skipsProcessing() {
        TaskCompletedEventPayload payload = TaskCompletedEventPayload.builder()
                .entityIdentifier(entityIdentifier).entityType(EntityType.LEAD)
                .taskConfigKey("TASK").stageKey("").build();

        listener.handleTaskCompletedEvent(new SystemEvent<>("TASK_COMPLETED", payload));

        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void handleTaskCompletedEvent_withValidPayload_callsProcessTaskCompletion() {
        TaskCompletedEventPayload payload = TaskCompletedEventPayload.builder()
                .entityIdentifier(entityIdentifier).entityType(EntityType.LEAD)
                .taskIdentifier(taskIdentifier).taskConfigKey("TASK")
                .outcome("APPROVED").stageKey("STAGE").assignedTo("user1").build();

        listener.handleTaskCompletedEvent(new SystemEvent<>("TASK_COMPLETED", payload));

        verify(workflowOrchestratorService).processTaskCompletion(
                entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user1");
    }

    @Test
    void handleTaskCompletedEvent_whenExceptionThrown_doesNotPropagate() {
        TaskCompletedEventPayload payload = TaskCompletedEventPayload.builder()
                .entityIdentifier(entityIdentifier).entityType(EntityType.LEAD)
                .taskIdentifier(taskIdentifier).taskConfigKey("TASK")
                .outcome("APPROVED").stageKey("STAGE").assignedTo("user1").build();
        doThrow(new RuntimeException("unexpected"))
                .when(workflowOrchestratorService).processTaskCompletion(any(), any(), any(), any(), any(), any(), any());

        listener.handleTaskCompletedEvent(new SystemEvent<>("TASK_COMPLETED", payload));

        verify(workflowOrchestratorService).processTaskCompletion(
                entityIdentifier, EntityType.LEAD, taskIdentifier, "TASK", "APPROVED", "STAGE", "user1");
    }
}
