package com.nivasafinance.features.leadstages.service.impl;

import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.StageTransitionEventPayload;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadstages.dto.CreateLeadStageHistoryRequest;
import com.nivasafinance.features.leadstages.dto.StageTransitionRequest;
import com.nivasafinance.features.leadstages.entity.LeadStageAssignmentHistory;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import com.nivasafinance.features.leadstages.exception.LeadStageHistoryValidationException;
import com.nivasafinance.features.leadstages.exception.LeadStageValidationException;
import com.nivasafinance.features.leadstages.repository.LeadStageHistoryRepositoryWrapper;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.workflow.dto.StageConfigResponse;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import com.nivasafinance.features.workflow.service.WorkflowConfigReadService;
import com.nivasafinance.features.workflow.exception.WorkflowConfigValidationException;
import com.nivasafinance.features.workflow.exception.WorkflowValidationException;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.leadstages.dto.BulkChangeAssignmentRequest;
import com.nivasafinance.features.leadstages.dto.BulkChangeAssignmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadStageHistoryWriteServiceImpl implements LeadStageHistoryWriteService {

    private final LeadStageHistoryRepositoryWrapper leadStageHistoryRepositoryWrapper;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final WorkflowOrchestratorService workflowOrchestratorService;
    private final WorkflowConfigReadService workflowConfigReadService;
    private final MessageSource messageSource;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(cacheNames = "leadDashboard", allEntries = true)
    public LeadStageHistory createInitialStage(UUID leadId, String workflowConfigKey) {
        ValidationUtils.requireNonNull(leadId, LeadStageValidationException::nullLeadId);
        ValidationUtils.requireNonNullOrEmpty(workflowConfigKey, WorkflowValidationException::nullOrEmptyWorkflowConfigKey);

        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        
        com.nivasafinance.features.workflow.entity.WorkflowConfig workflowConfig = 
                workflowConfigReadService.getWorkflowConfigByKey(workflowConfigKey);
        
        String landingStage = ValidationUtils.isNonNull(workflowConfig.getWorkflowConfigDetails()) 
                ? workflowConfig.getWorkflowConfigDetails().getLandingStage() 
                : null;
        
        if (!ValidationUtils.isNonNullOrEmpty(landingStage)) {
            log.error("Landing stage not configured for workflow: {}. Lead created but no initial stage assigned. LeadId: {}", 
                    workflowConfigKey, lead.getId());
            throw WorkflowConfigValidationException.landingStageNotFound(workflowConfigKey, messageSource);
        }

        // Update workflow config key on lead
        if (!ValidationUtils.isNonNull(lead.getWorkflowDetails())) {
            lead.setWorkflowDetails(new Lead.WorkflowDetails());
        }
        lead.getWorkflowDetails().setWorkflowConfigKey(workflowConfigKey);
        leadRepositoryWrapper.saveWithException(lead);

        // Get assignedTo and remarks from lead (if any)
        String assignedTo = null;
        String remarks = null;
        if (ValidationUtils.isNonNull(lead.getWorkflowDetails()) 
                && ValidationUtils.isNonNull(lead.getWorkflowDetails().getCurrentStageDetails())) {
            assignedTo = lead.getWorkflowDetails().getCurrentStageDetails().getAssignedTo();
        }
        if (ValidationUtils.isNonNull(lead.getWorkflowDetails()) 
                && ValidationUtils.isNonNull(lead.getWorkflowDetails().getLastStageDetails())) {
            remarks = lead.getWorkflowDetails().getLastStageDetails().getRemarks();
        }

        // Create initial stage entry
        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey(landingStage)
                .previousStageKey(null)
                .assignedTo(assignedTo)
                .remarks(remarks)
                .build();
        return createStageEntry(leadId, request);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(cacheNames = "leadDashboard", allEntries = true)
    public LeadStageHistory createStageEntry(UUID leadId, StageTransitionRequest request) {
        // Input validation
        ValidationUtils.requireNonNull(leadId, LeadStageValidationException::nullLeadId);
        ValidationUtils.requireNonNull(request, LeadStageValidationException::nullRequest);
        ValidationUtils.requireNonNullOrEmpty(request.getStageKey(), LeadStageValidationException::nullOrEmptyStageKey);
        
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        CreateLeadStageHistoryRequest createRequest = buildCreateLeadStageHistoryRequest(lead, request);
        return createStageEntryInternal(createRequest, lead);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(cacheNames = "leadDashboard", allEntries = true)
    public LeadStageHistory changeAssignment(UUID leadId, String stageKey, String newAssignedTo) {
        // Input validation
        ValidationUtils.requireNonNull(leadId, LeadStageValidationException::nullLeadId);
        ValidationUtils.requireNonNullOrEmpty(stageKey, LeadStageValidationException::nullOrEmptyStageKey);
        ValidationUtils.requireNonNullOrEmpty(newAssignedTo, LeadStageValidationException::nullOrEmptyAssignedTo);
        
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        return changeAssignmentInternal(lead.getId(), stageKey, newAssignedTo);
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(cacheNames = "leadDashboard", allEntries = true)
    public LeadStageHistory changeSubStage(UUID leadId, String stageKey, String subStageKey) {
        // Input validation
        ValidationUtils.requireNonNull(leadId, LeadStageValidationException::nullLeadId);
        ValidationUtils.requireNonNullOrEmpty(stageKey, LeadStageValidationException::nullOrEmptyStageKey);
        ValidationUtils.requireNonNullOrEmpty(subStageKey, LeadStageValidationException::nullOrEmptySubStageKey);
        
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        return changeSubStageInternal(lead.getId(), stageKey, subStageKey);
    }

    private LeadStageHistory createStageEntryInternal(CreateLeadStageHistoryRequest request, Lead lead) {
        validateCreateStageEntryRequest(request);

        Optional<LeadStageHistory> latestEntry = leadStageHistoryRepositoryWrapper
                .findLatestEntry(request.getLeadId());
        validatePreviousStageKey(request, latestEntry);

        if (latestEntry.isPresent()) {
            validateStageTransition(latestEntry.get(), request.getStageKey());
            validatePreviousEntryIsActive(latestEntry.get());
        }

        updateLeadWorkflowDetails(request.getLeadId(), request.getStageKey(), request.getAssignedTo(), LocalDateTime.now());

        LeadStageHistory savedHistory = saveStageHistory(request, latestEntry.orElse(null));

        // Publish event after transaction commits to ensure data consistency
        // For initial stage (when previousStageKey is null), set fromStageKey to "landing"
        String fromStageKeyForEvent = request.getPreviousStageKey() != null 
                ? request.getPreviousStageKey() 
                : com.nivasafinance.features.workflow.constants.WorkflowConstants.StageTask.LANDING_STAGE;
        
        StageTransitionEventPayload payload = StageTransitionEventPayload.builder()
                .entityType(EntityType.LEAD)
                .entityIdentifier(lead.getLeadIdentifier())
                .entityId(request.getLeadId())
                .fromStageKey(fromStageKeyForEvent)
                .toStageKey(request.getStageKey())
                .assignedTo(request.getAssignedTo())
                .remarks(request.getRemarks())
                .build();
        
        publishEventAfterCommit(
                new SystemEvent<>(BusinessEvent.STAGE_TRANSITIONED.toString(), payload)
        );

        return savedHistory;
    }

    private LeadStageHistory changeAssignmentInternal(Long leadId, String stageKey, String newAssignedTo) {
        validateChangeAssignmentRequest(leadId, stageKey, newAssignedTo);
        LeadStageHistory currentEntry = findActiveStageEntry(leadId, stageKey);
        LeadStageHistory updatedHistory = updateAssignment(currentEntry, newAssignedTo);
        updateLeadWorkflowDetailsAssignment(leadId, newAssignedTo);
        return updatedHistory;
    }

    private LeadStageHistory changeSubStageInternal(Long leadId, String stageKey, String subStageKey) {
        validateChangeSubStageRequest(leadId, stageKey, subStageKey);
        LeadStageHistory currentEntry = findActiveStageEntry(leadId, stageKey);
        LeadStageHistory updatedHistory = updateSubStage(currentEntry, subStageKey);
        updateLeadWorkflowDetailsSubStage(leadId, subStageKey);
        return updatedHistory;
    }

    private LeadStageHistory findActiveStageEntry(Long leadId, String stageKey) {
        LeadStageHistory currentEntry = leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)
                .orElseThrow(() -> LeadStageHistoryValidationException.noActiveStageEntry(leadId,
                        messageSource));

        validateStageKeyMatches(currentEntry, stageKey);
        validateStageIsActive(currentEntry, stageKey);

        return currentEntry;
    }

    private void validateStageKeyMatches(LeadStageHistory entry, String expectedStageKey) {
        if (!expectedStageKey.equals(entry.getStageKey())) {
            throw LeadStageHistoryValidationException.stageKeyMismatch(
                    expectedStageKey, entry.getStageKey(), messageSource);
        }
    }

    private void validateStageIsActive(LeadStageHistory entry, String stageKey) {
        if (ValidationUtils.isNonNull(entry.getExitedAt())) {
            throw LeadStageHistoryValidationException.stageAlreadyExited(stageKey, messageSource);
        }
    }

    private void updateLeadWorkflowDetails(Long leadId, String stageKey, String assignedTo, LocalDateTime enteredAt) {
        Lead lead = leadRepositoryWrapper.findByIdWithException(leadId);
        
        Lead.WorkflowDetails workflowDetails = lead.getWorkflowDetails();
        String existingWorkflowConfigKey = null;
        if (ValidationUtils.isNonNull(workflowDetails)) {
            existingWorkflowConfigKey = workflowDetails.getWorkflowConfigKey();
        }
        
        // Get default substage from workflow configuration if available
        String defaultSubStage = getDefaultSubStageForStage(existingWorkflowConfigKey, stageKey);
        
        Lead.CurrentStageDetails.CurrentStageDetailsBuilder builder = Lead.CurrentStageDetails.builder()
                .stageKey(stageKey)
                .subStageKey(defaultSubStage)
                .assignedTo(assignedTo)
                .enteredAt(enteredAt);
        
        // Only set assignedAt if assignedTo is not null
        if (ValidationUtils.isNonNull(assignedTo)) {
            builder.assignedAt(enteredAt);
        }
        
        Lead.CurrentStageDetails currentStageDetails = builder.build();
        
        workflowDetails = Lead.WorkflowDetails.builder()
                .workflowConfigKey(existingWorkflowConfigKey)
                .currentStageDetails(currentStageDetails)
                .build();
        
        lead.setWorkflowDetails(workflowDetails);
        leadRepositoryWrapper.saveWithException(lead);
    }
    
    /**
     * Gets the default substage for a stage from the workflow configuration.
     * Returns null if no default substage is configured.
     */
    private String getDefaultSubStageForStage(String workflowConfigKey, String stageKey) {
        try {
            if (!ValidationUtils.isNonNullOrEmpty(workflowConfigKey)) {
                return null;
            }
            return workflowOrchestratorService.getDefaultSubStageForStage(workflowConfigKey, stageKey);
        } catch (Exception e) {
            log.warn("Failed to get default substage for stage {} in workflow {}: {}. Proceeding without default substage.", 
                    stageKey, workflowConfigKey, e.getMessage());
            return null;
        }
    }

    private void validateCreateStageEntryRequest(CreateLeadStageHistoryRequest request) {
        leadRepositoryWrapper.findByIdWithException(request.getLeadId());
        
        Optional<LeadStageHistory> latestEntry = leadStageHistoryRepositoryWrapper
                .findLatestEntry(request.getLeadId());
        boolean hasExistingHistory = latestEntry.isPresent();
        
        // Delegate to workflow orchestrator for stage validation
        workflowOrchestratorService.validateStageTransition(
            request.getLeadId(),
            EntityType.LEAD,
            request.getStageKey(),
            request.getAssignedTo(),
            hasExistingHistory
        );
    }

    private void validatePreviousStageKey(CreateLeadStageHistoryRequest request,
            Optional<LeadStageHistory> latestEntry) {
        boolean hasExistingHistory = latestEntry.isPresent();
        boolean hasPreviousStageKey = ValidationUtils.isNonNull(request.getPreviousStageKey());

        if (!hasPreviousStageKey && hasExistingHistory) {
            throw LeadStageHistoryValidationException.previousStageKeyRequiredWhenHistoryExists(
                    request.getLeadId(), messageSource);
        }

        if (hasPreviousStageKey && !hasExistingHistory) {
            throw LeadStageHistoryValidationException.previousStageKeyProvidedForFirstEntry(
                    request.getPreviousStageKey(), messageSource);
        }
    }

    private LeadStageHistory saveStageHistory(CreateLeadStageHistoryRequest request, LeadStageHistory previousEntry) {
        if (ValidationUtils.isNonNull(previousEntry)) {
            closePreviousStageIfNeeded(request, previousEntry);
        }
        
        LeadStageHistory leadStageHistory = new LeadStageHistory();
        leadStageHistory.setLeadId(request.getLeadId());
        leadStageHistory.setStageKey(request.getStageKey());
        leadStageHistory.setStageFrom(ValidationUtils.isNonNull(previousEntry) ? request.getPreviousStageKey() : null);
        leadStageHistory.setEnteredAt(LocalDateTime.now());
        // Use "system" if username is null (for automated/system operations)
        String username = UserContext.getUsername();
        leadStageHistory.setMovedBy(username != null ? username : "system");
        leadStageHistory.setRemarks(request.getRemarks());
        
        LeadStageHistory savedHistory = leadStageHistoryRepositoryWrapper.save(leadStageHistory);
        
        if (ValidationUtils.isNonNull(request.getAssignedTo())) {
            LeadStageAssignmentHistory assignment = LeadStageAssignmentHistory.builder()
                    .assignedTo(request.getAssignedTo())
                    .assignedAt(LocalDateTime.now())
                    .assignedBy(username != null ? username : "system")
                    .build();
            assignment.setStageHistory(savedHistory);
            savedHistory.getAssignmentHistory().add(assignment);
            savedHistory = leadStageHistoryRepositoryWrapper.save(savedHistory);
        }
        
        return savedHistory;
    }

    private void closePreviousStageIfNeeded(CreateLeadStageHistoryRequest request, LeadStageHistory previousEntry) {
        String previousStageKey = request.getPreviousStageKey();
        if (!ValidationUtils.isNonNull(previousStageKey)
                || !previousStageKey.equals(previousEntry.getStageKey())) {
            throw LeadStageHistoryValidationException.stageFromMismatch(
                    previousStageKey, previousEntry.getStageKey(), messageSource);
        }

        if (!ValidationUtils.isNonNull(previousEntry.getExitedAt())) {
            previousEntry.setExitedAt(LocalDateTime.now());
            unassignActiveAssignments(previousEntry);
            leadStageHistoryRepositoryWrapper.save(previousEntry);
        }
    }

    private void unassignActiveAssignments(LeadStageHistory stageHistory) {
        LocalDateTime now = LocalDateTime.now();
        if (ValidationUtils.isNonNull(stageHistory.getAssignmentHistory())) {
            stageHistory.getAssignmentHistory().stream()
                    .filter(assignment -> !ValidationUtils.isNonNull(assignment.getUnassignedAt()))
                    .forEach(assignment -> assignment.setUnassignedAt(now));
        }
    }

    private void validateStageTransition(LeadStageHistory previousEntry, String newStageKey) {
        StageConfigResponse previousStageConfig = workflowOrchestratorService.getStageConfig(previousEntry.getStageKey());
        List<String> possibleNextStages = previousStageConfig.getPossibleNextStages();

        if (ValidationUtils.isNonNull(possibleNextStages) && !possibleNextStages.contains(newStageKey)) {
            throw LeadStageHistoryValidationException.invalidStageTransition(
                    previousEntry.getStageKey(), newStageKey, messageSource);
        }
    }

    private void validatePreviousEntryIsActive(LeadStageHistory previousEntry) {
        if (ValidationUtils.isNonNull(previousEntry.getExitedAt())) {
            throw LeadStageHistoryValidationException.stageAlreadyExited(
                    previousEntry.getStageKey(), messageSource);
        }
    }

    private void validateChangeAssignmentRequest(Long leadId, String stageKey, String newAssignedTo) {
        leadRepositoryWrapper.findByIdWithException(leadId);
    }

    private void validateChangeSubStageRequest(Long leadId, String stageKey, String subStageKey) {
        leadRepositoryWrapper.findByIdWithException(leadId);
        StageConfigResponse stageConfig = workflowOrchestratorService.getStageConfig(stageKey);
        
        boolean isValidSubStage = ValidationUtils.isNonNull(stageConfig.getSubStages()) && 
                stageConfig.getSubStages().stream()
                        .anyMatch(subStage -> subStageKey.equals(subStage.getKey()));
        
        if (!isValidSubStage) {
            throw LeadStageHistoryValidationException.invalidSubStageKey(subStageKey, stageKey, messageSource);
        }
    }

    private LeadStageHistory updateAssignment(LeadStageHistory currentEntry, String newAssignedTo) {
        // Use "system" if username is null (for automated/system operations)
        String username = UserContext.getUsername();
        LeadStageAssignmentHistory assignment = LeadStageAssignmentHistory.builder()
                .assignedTo(newAssignedTo)
                .assignedAt(LocalDateTime.now())
                .assignedBy(username != null ? username : "system")
                .build();
        assignment.setStageHistory(currentEntry);
        currentEntry.getAssignmentHistory().add(assignment);
        return leadStageHistoryRepositoryWrapper.save(currentEntry);
    }

    private LeadStageHistory updateSubStage(LeadStageHistory currentEntry, String subStageKey) {
        currentEntry.setSubStageKey(subStageKey);
        return leadStageHistoryRepositoryWrapper.save(currentEntry);
    }

    private void updateLeadWorkflowDetailsSubStage(Long leadId, String subStageKey) {
        Lead lead = leadRepositoryWrapper.findByIdWithException(leadId);
        
        Lead.WorkflowDetails workflowDetails = lead.getWorkflowDetails();
        if (!ValidationUtils.isNonNull(workflowDetails) || !ValidationUtils.isNonNull(workflowDetails.getCurrentStageDetails())) {
            // If workflow details don't exist, we can't update substage
            log.warn("Cannot update substage for lead {} - workflow details or current stage details are null", leadId);
            return;
        }
        
        Lead.CurrentStageDetails currentStageDetails = workflowDetails.getCurrentStageDetails();
        Lead.CurrentStageDetails updatedCurrentStageDetails = Lead.CurrentStageDetails.builder()
                .stageKey(currentStageDetails.getStageKey())
                .subStageKey(subStageKey)
                .assignedTo(currentStageDetails.getAssignedTo())
                .assignedAt(currentStageDetails.getAssignedAt())
                .enteredAt(currentStageDetails.getEnteredAt())
                .build();
        
        Lead.WorkflowDetails updatedWorkflowDetails = Lead.WorkflowDetails.builder()
                .workflowConfigKey(workflowDetails.getWorkflowConfigKey())
                .currentStageDetails(updatedCurrentStageDetails)
                .lastStageDetails(workflowDetails.getLastStageDetails())
                .build();
        
        lead.setWorkflowDetails(updatedWorkflowDetails);
        leadRepositoryWrapper.saveWithException(lead);
    }

    private void updateLeadWorkflowDetailsAssignment(Long leadId, String newAssignedTo) {
        Lead lead = leadRepositoryWrapper.findByIdWithException(leadId);
        
        Lead.WorkflowDetails workflowDetails = lead.getWorkflowDetails();
        if (!ValidationUtils.isNonNull(workflowDetails) || !ValidationUtils.isNonNull(workflowDetails.getCurrentStageDetails())) {
            // If workflow details don't exist, we can't update assignment
            log.warn("Cannot update assignment for lead {} - workflow details or current stage details are null", leadId);
            return;
        }
        
        Lead.CurrentStageDetails currentStageDetails = workflowDetails.getCurrentStageDetails();
        LocalDateTime now = LocalDateTime.now();
        Lead.CurrentStageDetails updatedCurrentStageDetails = Lead.CurrentStageDetails.builder()
                .stageKey(currentStageDetails.getStageKey())
                .subStageKey(currentStageDetails.getSubStageKey())
                .assignedTo(newAssignedTo)
                .assignedAt(newAssignedTo != null ? now : currentStageDetails.getAssignedAt())
                .enteredAt(currentStageDetails.getEnteredAt())
                .build();
        
        Lead.WorkflowDetails updatedWorkflowDetails = Lead.WorkflowDetails.builder()
                .workflowConfigKey(workflowDetails.getWorkflowConfigKey())
                .currentStageDetails(updatedCurrentStageDetails)
                .lastStageDetails(workflowDetails.getLastStageDetails())
                .build();
        
        lead.setWorkflowDetails(updatedWorkflowDetails);
        leadRepositoryWrapper.saveWithException(lead);
    }

    private CreateLeadStageHistoryRequest buildCreateLeadStageHistoryRequest(Lead lead, StageTransitionRequest request) {
        Optional<LeadStageHistory> latestEntry = leadStageHistoryRepositoryWrapper.findLatestEntry(lead.getId());
        String previousStageKey = latestEntry.map(LeadStageHistory::getStageKey).orElse(null);
        
        return CreateLeadStageHistoryRequest.builder()
                .leadId(lead.getId())
                .stageKey(request.getStageKey())
                .previousStageKey(previousStageKey != null ? previousStageKey : request.getPreviousStageKey())
                .assignedTo(request.getAssignedTo())
                .remarks(request.getRemarks())
                .build();
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(cacheNames = "leadDashboard", allEntries = true)
    public BulkChangeAssignmentResponse bulkChangeAssignment(BulkChangeAssignmentRequest request) {
        // Input validation
        ValidationUtils.requireNonNull(request, LeadStageValidationException::nullRequest);
        ValidationUtils.requireNonNullOrEmpty(request.getStageKey(), LeadStageValidationException::nullOrEmptyStageKey);
        ValidationUtils.requireNonNullOrEmpty(request.getNewAssignedTo(), LeadStageValidationException::nullOrEmptyAssignedTo);
        ValidationUtils.requireNonNullOrEmpty(request.getLeadIdentifiers(), LeadStageValidationException::nullOrEmptyLeadIdentifiers);
        List<UUID> successfulLeadIdentifiers = new ArrayList<>();
        List<BulkChangeAssignmentResponse.BulkAssignmentError> errors = new ArrayList<>();
        
        for (UUID leadIdentifier : request.getLeadIdentifiers()) {
            try {
                // Process assignment synchronously to get immediate results
                changeAssignment(leadIdentifier, request.getStageKey(), request.getNewAssignedTo());
                successfulLeadIdentifiers.add(leadIdentifier);
                log.debug("Successfully assigned lead {} to {} at stage {}", 
                        leadIdentifier, request.getNewAssignedTo(), request.getStageKey());
            } catch (Exception e) {
                // Track failed assignments
                BulkChangeAssignmentResponse.BulkAssignmentError error = 
                        BulkChangeAssignmentResponse.BulkAssignmentError.builder()
                                .leadIdentifier(leadIdentifier)
                                .errorMessage(e.getMessage() != null ? e.getMessage() : "Unknown error")
                                .build();
                errors.add(error);
                log.warn("Failed to assign lead {} to {} at stage {}: {}", 
                        leadIdentifier, request.getNewAssignedTo(), request.getStageKey(), e.getMessage(), e);
            }
        }
        
        int totalRequested = request.getLeadIdentifiers().size();
        int successful = successfulLeadIdentifiers.size();
        int failed = errors.size();
        
        return BulkChangeAssignmentResponse.builder()
                .totalRequested(totalRequested)
                .successful(successful)
                .failed(failed)
                .successfulLeadIdentifiers(successfulLeadIdentifiers)
                .errors(errors)
                .build();
    }

    /**
     * Publishes event after transaction commits to ensure data consistency.
     * If transaction rolls back, event will not be published.
     */
    private void publishEventAfterCommit(SystemEvent<?> event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            eventPublisher.publishEvent(event);
                        }
                    }
            );
        } else {
            // No active transaction, publish immediately
            eventPublisher.publishEvent(event);
        }
    }
}