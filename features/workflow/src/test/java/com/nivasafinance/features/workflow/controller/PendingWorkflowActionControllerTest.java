package com.nivasafinance.features.workflow.controller;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.workflow.dto.PendingActionResponse;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import com.nivasafinance.features.workflow.service.PendingWorkflowActionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingWorkflowActionControllerTest {

    @Mock
    private PendingWorkflowActionService pendingWorkflowActionService;

    @Mock
    private WorkflowOrchestratorService workflowOrchestratorService;

    @InjectMocks
    private PendingWorkflowActionController controller;

    // ── getPendingActions: by sourceTaskIdentifier ──

    @Test
    void getPendingActions_withSourceTaskIdentifier_delegatesToServiceBySourceTask() {
        UUID taskId = UUID.randomUUID();
        List<PendingActionResponse> expected = List.of(new PendingActionResponse());
        when(pendingWorkflowActionService.getPendingActionsBySourceTask(taskId)).thenReturn(expected);

        ResponseEntity<List<PendingActionResponse>> result =
                controller.getPendingActions(taskId, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode(),
                "Should return 200 OK when querying by source task identifier");
        assertEquals(expected, result.getBody(),
                "Should return the list from service when querying by source task");
        verify(pendingWorkflowActionService).getPendingActionsBySourceTask(taskId);
        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void getPendingActions_withSourceTaskIdentifier_takesPrecedenceOverEntity() {
        UUID taskId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        when(pendingWorkflowActionService.getPendingActionsBySourceTask(taskId))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<PendingActionResponse>> result =
                controller.getPendingActions(taskId, entityId, EntityType.LEAD);

        assertEquals(HttpStatus.OK, result.getStatusCode(),
                "sourceTaskIdentifier should take precedence when both params are provided");
        verify(pendingWorkflowActionService).getPendingActionsBySourceTask(taskId);
        verify(pendingWorkflowActionService, never()).getPendingActionsByEntity(any(), any());
    }

    // ── getPendingActions: by entity ──

    @Test
    void getPendingActions_withEntityIdentifierAndType_delegatesToServiceByEntity() {
        UUID entityId = UUID.randomUUID();
        List<PendingActionResponse> expected = List.of(new PendingActionResponse());
        when(pendingWorkflowActionService.getPendingActionsByEntity(entityId, EntityType.LEAD))
                .thenReturn(expected);

        ResponseEntity<List<PendingActionResponse>> result =
                controller.getPendingActions(null, entityId, EntityType.LEAD);

        assertEquals(HttpStatus.OK, result.getStatusCode(),
                "Should return 200 OK when querying by entity identifier and type");
        assertEquals(expected, result.getBody(),
                "Should return the list from service when querying by entity");
    }

    // ── getPendingActions: bad request ──

    @Test
    void getPendingActions_withNoParams_returnsBadRequest() {
        ResponseEntity<List<PendingActionResponse>> result =
                controller.getPendingActions(null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode(),
                "Should return 400 BAD REQUEST when no query parameters are provided");
    }

    @Test
    void getPendingActions_withEntityIdButNoType_returnsBadRequest() {
        ResponseEntity<List<PendingActionResponse>> result =
                controller.getPendingActions(null, UUID.randomUUID(), null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode(),
                "Should return 400 BAD REQUEST when entityIdentifier is provided without entityType");
    }

    @Test
    void getPendingActions_withEntityTypeButNoId_returnsBadRequest() {
        ResponseEntity<List<PendingActionResponse>> result =
                controller.getPendingActions(null, null, EntityType.LEAD);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode(),
                "Should return 400 BAD REQUEST when entityType is provided without entityIdentifier");
    }

    // ── executePendingAction ──

    @Test
    void executePendingAction_delegatesToOrchestrator() {
        UUID actionId = UUID.randomUUID();
        LocalDateTime dueDate = LocalDateTime.of(2026, 6, 1, 10, 0);
        com.nivasafinance.features.workflow.dto.ExecutePendingActionRequest request =
                com.nivasafinance.features.workflow.dto.ExecutePendingActionRequest.builder()
                        .actionIdentifier(actionId)
                        .assignTo("user1")
                        .dueDate(dueDate)
                        .build();

        ResponseEntity<Void> result = controller.executePendingAction(request);

        assertEquals(HttpStatus.OK, result.getStatusCode(),
                "Should return 200 OK after executing pending action");
        verify(workflowOrchestratorService).executePendingAction(actionId, "user1", dueDate);
    }

    @Test
    void executePendingAction_withNullOptionalFields_delegatesNulls() {
        UUID actionId = UUID.randomUUID();
        com.nivasafinance.features.workflow.dto.ExecutePendingActionRequest request =
                com.nivasafinance.features.workflow.dto.ExecutePendingActionRequest.builder()
                        .actionIdentifier(actionId)
                        .assignTo(null)
                        .dueDate(null)
                        .build();

        ResponseEntity<Void> result = controller.executePendingAction(request);

        assertEquals(HttpStatus.OK, result.getStatusCode(),
                "Should return 200 OK even with null optional fields");
        verify(workflowOrchestratorService).executePendingAction(actionId, null, null);
    }

    // ── cancelPendingAction ──

    @Test
    void cancelPendingAction_delegatesToOrchestrator() {
        UUID actionId = UUID.randomUUID();

        ResponseEntity<Void> result = controller.cancelPendingAction(actionId);

        assertEquals(HttpStatus.OK, result.getStatusCode(),
                "Should return 200 OK after cancelling pending action");
        verify(workflowOrchestratorService).cancelPendingAction(actionId);
    }

    // ── cancelPendingActionsBySourceTask ──

    @Test
    void cancelPendingActionsBySourceTask_delegatesToOrchestrator() {
        UUID taskId = UUID.randomUUID();

        ResponseEntity<Void> result = controller.cancelPendingActionsBySourceTask(taskId);

        assertEquals(HttpStatus.OK, result.getStatusCode(),
                "Should return 200 OK after cancelling pending actions by source task");
        verify(workflowOrchestratorService).cancelPendingActionsBySourceTask(taskId);
    }
}
