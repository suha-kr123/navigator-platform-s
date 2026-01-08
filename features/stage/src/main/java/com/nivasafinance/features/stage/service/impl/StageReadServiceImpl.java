package com.nivasafinance.features.stage.service.impl;

import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.rolemanagement.role.service.UserQueryService;
import com.nivasafinance.features.stage.dto.StageConfigResponse;
import com.nivasafinance.features.stage.dto.StageFilterResponse;
import com.nivasafinance.features.stage.dto.StageTemplateResponse;
import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.repository.StageConfigRepositoryWrapper;
import com.nivasafinance.features.stage.service.StageReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class StageReadServiceImpl implements StageReadService {

    private final StageConfigRepositoryWrapper stageConfigRepositoryWrapper;
    private final CodeMasterService codeMasterService;
    private final UserQueryService userQueryService;

    @Override
    public StageConfigResponse getStageByKey(String key) {
        StageConfig stageConfig = stageConfigRepositoryWrapper.findByKeyWithException(key);
        
        StageConfig.StageConfigDetails stageConfigDetails = stageConfig.getStageConfig();
        List<String> possibleNextStages = ValidationUtils.isNonNull(stageConfigDetails) 
                && ValidationUtils.isNonNull(stageConfigDetails.getPossibleNextStages())
                ? stageConfigDetails.getPossibleNextStages() 
                : Collections.emptyList();
        
        StageConfig.AssigneeRoles assigneeRolesEntity = stageConfig.getAssigneeRoles();
        List<String> assigneeRoles = ValidationUtils.isNonNull(assigneeRolesEntity) 
                && ValidationUtils.isNonNull(assigneeRolesEntity.getRoles())
                ? assigneeRolesEntity.getRoles() 
                : Collections.emptyList();
        
        List<CodeValueResponse> subStages = fetchSubStages(stageConfig.getSubStagesCode());
        
        return StageConfigResponse.from(stageConfig, possibleNextStages, assigneeRoles, subStages);
    }

    private List<CodeValueResponse> fetchSubStages(String subStagesCode) {
        if (!ValidationUtils.isNonNullOrEmpty(subStagesCode)) {
            return Collections.emptyList();
        }
        
        try {
            List<CodeValueResponse> codeValues = codeMasterService.getAllCodeValuesByCodeKey(subStagesCode, true);
            return codeValues != null ? codeValues : Collections.emptyList();
        } catch (Exception e) {
            // Log error but return empty list to prevent failure
            log.warn("Failed to fetch sub stages for code: {}, error: {}", subStagesCode, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public StageTemplateResponse getStageTemplate(String stageKey) {
        StageConfigResponse stageConfig = getStageByKey(stageKey);
        
        return StageTemplateResponse.builder()
                .stageKey(stageConfig.getKey())
                .stageName(stageConfig.getName())
                .stageDescription(stageConfig.getDescription())
                .possibleNextStages(stageConfig.getPossibleNextStages())
                .availableSubStages(stageConfig.getSubStages())
                .build();
    }

    @Override
    public Map<String, StageTemplateResponse> getStageTemplates(List<String> stageKeys) {
        try {
            List<String> processedStageKeys = processStageKeys(stageKeys);
            if (processedStageKeys.isEmpty()) {
                return Collections.emptyMap();
            }
            
            Map<String, StageTemplateResponse> result = new HashMap<>();
            
            for (String stageKey : processedStageKeys) {
                try {
                    StageTemplateResponse template = getStageTemplate(stageKey);
                    if (template != null) {
                        result.put(stageKey, template);
                    }
                } catch (ResourceNotFoundException e) {
                    // Stage not found - skip and continue
                    log.debug("Stage not found: {}, skipping", stageKey);
                    continue;
                } catch (Exception e) {
                    // Skip invalid stage keys and continue processing others
                    log.warn("Failed to get template for stage key: {}, error: {}", stageKey, e.getMessage(), e);
                    continue;
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("Unexpected error in getStageTemplates: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public List<UserAssignmentResponse> getAssignableUsersForStages(List<String> stageKeys, String officeKey) {
        try {
            List<String> processedStageKeys = processStageKeys(stageKeys);
            if (processedStageKeys.isEmpty()) {
                return Collections.emptyList();
            }
            
            if (!ValidationUtils.isNonNull(officeKey)) {
                return Collections.emptyList();
            }
            
            // Collect all unique assignee roles from all stages
            Set<String> allRoles = new HashSet<>();
            for (String stageKey : processedStageKeys) {
                try {
                    StageConfigResponse stageConfig = getStageByKey(stageKey);
                    if (stageConfig != null && !CollectionUtils.isEmpty(stageConfig.getAssigneeRoles())) {
                        allRoles.addAll(stageConfig.getAssigneeRoles());
                    }
                } catch (Exception e) {
                    // Skip invalid stage keys and continue processing others
                    log.debug("Failed to get stage config for key: {}, error: {}", stageKey, e.getMessage());
                    continue;
                }
            }
            
            if (allRoles.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Get users for all roles based on provided office key
            List<UserAssignmentResponse> allUsers = userQueryService.getUsersByOfficeAndRoles(new ArrayList<>(allRoles), officeKey);
            
            // Deduplicate by username to avoid returning same user multiple times
            Map<String, UserAssignmentResponse> uniqueUsers = allUsers.stream()
                    .collect(Collectors.toMap(
                            UserAssignmentResponse::getUsername,
                            user -> user,
                            (existing, replacement) -> existing
                    ));
            
            return new ArrayList<>(uniqueUsers.values());
        } catch (Exception e) {
            log.error("Unexpected error in getAssignableUsersForStages: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Processes and normalizes stage keys: filters nulls, trims whitespace, removes empty strings, and deduplicates.
     */
    private List<String> processStageKeys(List<String> stageKeys) {
        if (stageKeys == null || stageKeys.isEmpty()) {
            return Collections.emptyList();
        }
        
        return stageKeys.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(key -> !key.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public List<StageFilterResponse> getAllActiveStages() {
        return stageConfigRepositoryWrapper.findAllActiveStages().stream()
                .map(StageFilterResponse::from)
                .collect(Collectors.toList());
    }
}
