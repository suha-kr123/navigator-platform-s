package com.nivasafinance.features.leadstages.service.impl;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadstages.dto.CreateLeadStageHistoryRequest;
import com.nivasafinance.features.leadstages.entity.LeadStageAssignmentHistory;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import com.nivasafinance.features.leadstages.repository.LeadStageHistoryRepositoryWrapper;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import com.nivasafinance.features.leadstages.exception.LeadStageHistoryValidationException;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.stage.dto.StageConfigResponse;
import com.nivasafinance.features.stage.service.StageReadService;
import com.nivasafinance.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadStageHistoryWriteServiceImpl implements LeadStageHistoryWriteService {

        private final LeadStageHistoryRepositoryWrapper leadStageHistoryRepositoryWrapper;
        private final LeadRepositoryWrapper leadRepositoryWrapper;
        private final StageReadService stageReadService;
        private final UserRoleService userRoleService;
        private final MessageSource messageSource;

        @Override
        @Transactional
        public LeadStageHistory createStageEntry(CreateLeadStageHistoryRequest request) {
                validateCreateStageEntryRequest(request);
                Optional<LeadStageHistory> latestEntryOptional = leadStageHistoryRepositoryWrapper.findLatestEntry(request.getLeadId());
                validatePreviousStageKey(request, latestEntryOptional);
                
                if (latestEntryOptional.isPresent()) {
                        validateStageTransition(latestEntryOptional.get(), request.getStageKey());
                        validatePreviousEntryIsActive(latestEntryOptional.get());
                }
                
                LeadStageHistory leadStageHistory = latestEntryOptional.isPresent()
                        ? buildLeadStageHistory(request, latestEntryOptional.get())
                        : buildLeadStageHistory(request, null);
                        
                LeadStageHistory savedHistory = leadStageHistoryRepositoryWrapper.save(leadStageHistory);
                
                if (ValidationUtils.isNonNull(request.getAssignedTo())) {
                        createInitialAssignment(savedHistory, request.getAssignedTo());
                }
                
                return savedHistory;
        }

        @Override
        @Transactional
        public LeadStageHistory changeAssignment(Long leadId, String stageKey, String newAssignedTo) {
                validateChangeAssignmentRequest(leadId, stageKey, newAssignedTo);
                LeadStageHistory currentEntry = leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)
                        .orElseThrow(() -> LeadStageHistoryValidationException.noActiveStageEntry(leadId, messageSource));
                
                if (!stageKey.equals(currentEntry.getStageKey())) {
                        throw LeadStageHistoryValidationException.stageKeyMismatch(stageKey, currentEntry.getStageKey(), messageSource);
                }
                
                if (ValidationUtils.isNonNull(currentEntry.getExitedAt())) {
                        throw LeadStageHistoryValidationException.stageAlreadyExited(stageKey, messageSource);
                }
                
                return updateAssignment(currentEntry, newAssignedTo);
        }

        @Override
        @Transactional
        public LeadStageHistory changeSubStage(Long leadId, String stageKey, String subStageKey) {
                validateChangeSubStageRequest(leadId, stageKey, subStageKey);
                LeadStageHistory currentEntry = leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)
                        .orElseThrow(() -> LeadStageHistoryValidationException.noActiveStageEntry(leadId, messageSource));
                
                if (!stageKey.equals(currentEntry.getStageKey())) {
                        throw LeadStageHistoryValidationException.stageKeyMismatch(stageKey, currentEntry.getStageKey(), messageSource);
                }
                
                if (ValidationUtils.isNonNull(currentEntry.getExitedAt())) {
                        throw LeadStageHistoryValidationException.stageAlreadyExited(stageKey, messageSource);
                }
                
                return updateSubStage(currentEntry, subStageKey);
        }

        private void validateCreateStageEntryRequest(CreateLeadStageHistoryRequest request) {
                leadRepositoryWrapper.findByIdWithException(request.getLeadId());
                StageConfigResponse stageConfig = stageReadService.getStageByKey(request.getStageKey());
                List<String> allowedRoles = stageConfig.getAssigneeRoles();
                List<String> userRoles = userRoleService.getRolesByUsername(request.getAssignedTo());

                boolean isAssigneeAllowed = ValidationUtils.isNonNull(allowedRoles) && allowedRoles.stream().anyMatch(userRoles::contains);
                if (!isAssigneeAllowed) {
                        throw LeadStageHistoryValidationException.userNotInAssigneeRoles(request.getAssignedTo(), allowedRoles, messageSource);
                }
        }

        private void validatePreviousStageKey(CreateLeadStageHistoryRequest request, Optional<LeadStageHistory> latestEntryOptional) {
                boolean hasExistingHistory = latestEntryOptional.isPresent();
                
                if (!ValidationUtils.isNonNull(request.getPreviousStageKey()) && hasExistingHistory) {
                        throw LeadStageHistoryValidationException.previousStageKeyRequiredWhenHistoryExists(request.getLeadId(), messageSource);
                }
                
                if (ValidationUtils.isNonNull(request.getPreviousStageKey()) && !hasExistingHistory) {
                        throw LeadStageHistoryValidationException.previousStageKeyProvidedForFirstEntry(request.getPreviousStageKey(), messageSource);
                }
        }

        private LeadStageHistory buildLeadStageHistory(CreateLeadStageHistoryRequest request, LeadStageHistory previousEntry) {
                if (ValidationUtils.isNonNull(previousEntry)) {
                        closePreviousStageIfNeeded(request, previousEntry);
                }
                
                LeadStageHistory leadStageHistory = new LeadStageHistory();
                leadStageHistory.setLeadId(request.getLeadId());
                leadStageHistory.setStageKey(request.getStageKey());
                leadStageHistory.setStageFrom(ValidationUtils.isNonNull(previousEntry) ? request.getPreviousStageKey() : null);
                leadStageHistory.setEnteredAt(LocalDateTime.now());
                leadStageHistory.setMovedBy(UserContext.getCurrentUsername());
                leadStageHistory.setRemarks(request.getRemarks());
                return leadStageHistory;
        }

        private void closePreviousStageIfNeeded(CreateLeadStageHistoryRequest request, LeadStageHistory previousEntry) {
                String previousStageKey = request.getPreviousStageKey();
                if (!ValidationUtils.isNonNull(previousStageKey) || !previousStageKey.equals(previousEntry.getStageKey())) {
                        throw LeadStageHistoryValidationException.stageFromMismatch(previousStageKey,
                                        previousEntry.getStageKey(), messageSource);
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
                StageConfigResponse previousStageConfig = stageReadService.getStageByKey(previousEntry.getStageKey());
                List<String> possibleNextStages = previousStageConfig.getPossibleNextStages();
                
                if (ValidationUtils.isNonNull(possibleNextStages) && !possibleNextStages.contains(newStageKey)) {
                        throw LeadStageHistoryValidationException.invalidStageTransition(previousEntry.getStageKey(), newStageKey, messageSource);
                }
        }

        private void validatePreviousEntryIsActive(LeadStageHistory previousEntry) {
                if (ValidationUtils.isNonNull(previousEntry.getExitedAt())) {
                        throw LeadStageHistoryValidationException.stageAlreadyExited(previousEntry.getStageKey(), messageSource);
                }
        }

        private void createInitialAssignment(LeadStageHistory leadStageHistory, String assignedTo) {
                LeadStageAssignmentHistory assignment = LeadStageAssignmentHistory.builder()
                        .assignedTo(assignedTo)
                        .assignedAt(LocalDateTime.now())
                        .assignedBy(UserContext.getCurrentUsername())
                        .build();
                assignment.setStageHistory(leadStageHistory);
                leadStageHistory.getAssignmentHistory().add(assignment);
                leadStageHistoryRepositoryWrapper.save(leadStageHistory);
        }

        private void validateChangeAssignmentRequest(Long leadId, String stageKey, String newAssignedTo) {
                leadRepositoryWrapper.findByIdWithException(leadId);
                StageConfigResponse stageConfig = stageReadService.getStageByKey(stageKey);
                List<String> allowedRoles = stageConfig.getAssigneeRoles();
                List<String> userRoles = userRoleService.getRolesByUsername(newAssignedTo);

                boolean isAssigneeAllowed = ValidationUtils.isNonNull(allowedRoles) && allowedRoles.stream().anyMatch(userRoles::contains);
                if (!isAssigneeAllowed) {
                        throw LeadStageHistoryValidationException.userNotInAssigneeRoles(newAssignedTo, allowedRoles, messageSource);
                }
        }

        private void validateChangeSubStageRequest(Long leadId, String stageKey, String subStageKey) {
                leadRepositoryWrapper.findByIdWithException(leadId);
                StageConfigResponse stageConfig = stageReadService.getStageByKey(stageKey);
                
                boolean isValidSubStage = ValidationUtils.isNonNull(stageConfig.getSubStages()) && 
                        stageConfig.getSubStages().stream()
                                .anyMatch(subStage -> subStageKey.equals(subStage.getKey()));
                
                if (!isValidSubStage) {
                        throw LeadStageHistoryValidationException.invalidSubStageKey(subStageKey, stageKey, messageSource);
                }
        }

        private LeadStageHistory updateAssignment(LeadStageHistory currentEntry, String newAssignedTo) {
                LeadStageAssignmentHistory assignment = LeadStageAssignmentHistory.builder()
                        .assignedTo(newAssignedTo)
                        .assignedAt(LocalDateTime.now())
                        .assignedBy(UserContext.getCurrentUsername())
                        .build();
                assignment.setStageHistory(currentEntry);
                currentEntry.getAssignmentHistory().add(assignment);
                return leadStageHistoryRepositoryWrapper.save(currentEntry);
        }

        private LeadStageHistory updateSubStage(LeadStageHistory currentEntry, String subStageKey) {
                currentEntry.setSubStageKey(subStageKey);
                return leadStageHistoryRepositoryWrapper.save(currentEntry);
        }

}