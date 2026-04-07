package com.nivasafinance.features.workflow.repository;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.workflow.entity.PendingWorkflowAction;
import com.nivasafinance.features.workflow.exception.WorkflowConfigNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PendingWorkflowActionRepositoryWrapper {

    private final PendingWorkflowActionRepository repository;
    private final MessageSource messageSource;

    public PendingWorkflowAction save(PendingWorkflowAction action) {
        return repository.save(action);
    }

    public List<PendingWorkflowAction> findPendingBySourceTask(UUID sourceTaskIdentifier) {
        return repository.findBySourceTaskIdentifierAndStatus(
                sourceTaskIdentifier, PendingWorkflowAction.STATUS_PENDING);
    }

    public List<PendingWorkflowAction> findPendingByEntity(UUID entityIdentifier, EntityType entityType) {
        return repository.findByEntityIdentifierAndEntityTypeAndStatus(
                entityIdentifier, entityType, PendingWorkflowAction.STATUS_PENDING);
    }

    public PendingWorkflowAction findByActionIdentifierWithException(UUID actionIdentifier) {
        return repository.findByActionIdentifier(actionIdentifier)
                .orElseThrow(() -> WorkflowConfigNotFoundException.workflowConfigNotFound(
                        actionIdentifier.toString(), messageSource));
    }
}
