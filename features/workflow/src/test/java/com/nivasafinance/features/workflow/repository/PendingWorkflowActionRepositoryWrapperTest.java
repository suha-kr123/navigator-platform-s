package com.nivasafinance.features.workflow.repository;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.workflow.entity.PendingWorkflowAction;
import com.nivasafinance.features.workflow.exception.WorkflowConfigNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingWorkflowActionRepositoryWrapperTest {

    @Mock
    private PendingWorkflowActionRepository repository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private PendingWorkflowActionRepositoryWrapper wrapper;

    // ── save ──

    @Test
    void save_delegatesToRepository() {
        PendingWorkflowAction action = new PendingWorkflowAction();
        when(repository.save(action)).thenReturn(action);

        PendingWorkflowAction result = wrapper.save(action);

        assertSame(action, result, "Saved entity should be the same instance returned by repository");
        verify(repository).save(action);
    }

    // ── findPendingBySourceTask ──

    @Test
    void findPendingBySourceTask_whenActionsExist_returnsList() {
        UUID taskId = UUID.randomUUID();
        PendingWorkflowAction action = new PendingWorkflowAction();
        when(repository.findBySourceTaskIdentifierAndStatus(taskId, PendingWorkflowAction.STATUS_PENDING))
                .thenReturn(List.of(action));

        List<PendingWorkflowAction> result = wrapper.findPendingBySourceTask(taskId);

        assertEquals(1, result.size(), "Should return actions matching source task and PENDING status");
    }

    @Test
    void findPendingBySourceTask_whenNoActions_returnsEmptyList() {
        UUID taskId = UUID.randomUUID();
        when(repository.findBySourceTaskIdentifierAndStatus(taskId, PendingWorkflowAction.STATUS_PENDING))
                .thenReturn(Collections.emptyList());

        List<PendingWorkflowAction> result = wrapper.findPendingBySourceTask(taskId);

        assertTrue(result.isEmpty(), "Should return empty list when no pending actions for source task");
    }

    // ── findPendingByEntity ──

    @Test
    void findPendingByEntity_whenActionsExist_returnsList() {
        UUID entityId = UUID.randomUUID();
        PendingWorkflowAction action = new PendingWorkflowAction();
        when(repository.findByEntityIdentifierAndEntityTypeAndStatus(
                entityId, EntityType.LEAD, PendingWorkflowAction.STATUS_PENDING))
                .thenReturn(List.of(action));

        List<PendingWorkflowAction> result = wrapper.findPendingByEntity(entityId, EntityType.LEAD);

        assertEquals(1, result.size(), "Should return actions matching entity and PENDING status");
    }

    @Test
    void findPendingByEntity_whenNoActions_returnsEmptyList() {
        UUID entityId = UUID.randomUUID();
        when(repository.findByEntityIdentifierAndEntityTypeAndStatus(
                entityId, EntityType.LEAD, PendingWorkflowAction.STATUS_PENDING))
                .thenReturn(Collections.emptyList());

        List<PendingWorkflowAction> result = wrapper.findPendingByEntity(entityId, EntityType.LEAD);

        assertTrue(result.isEmpty(), "Should return empty list when no pending actions for entity");
    }

    // ── findByActionIdentifierWithException ──

    @Test
    void findByActionIdentifierWithException_whenFound_returnsAction() {
        UUID actionId = UUID.randomUUID();
        PendingWorkflowAction action = new PendingWorkflowAction();
        when(repository.findByActionIdentifier(actionId)).thenReturn(Optional.of(action));

        PendingWorkflowAction result = wrapper.findByActionIdentifierWithException(actionId);

        assertSame(action, result, "Should return the action found by identifier");
    }

    @Test
    void findByActionIdentifierWithException_whenNotFound_throwsWorkflowConfigNotFoundException() {
        UUID actionId = UUID.randomUUID();
        when(repository.findByActionIdentifier(actionId)).thenReturn(Optional.empty());

        assertThrows(WorkflowConfigNotFoundException.class,
                () -> wrapper.findByActionIdentifierWithException(actionId),
                "Should throw WorkflowConfigNotFoundException when action identifier not found");
    }
}
