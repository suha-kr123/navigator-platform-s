package com.nivasafinance.features.stage.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.rolemanagement.role.service.UserQueryService;
import com.nivasafinance.features.rolemanagement.enums.Role;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.stage.dto.StageConfigResponse;
import com.nivasafinance.features.stage.dto.StageFilterResponse;
import com.nivasafinance.features.stage.dto.StageTemplateResponse;
import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.repository.StageConfigRepositoryWrapper;
import com.nivasafinance.features.stage.service.StageReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class StageReadServiceImpl implements StageReadService, ApplicationContextAware {

    private final StageConfigRepositoryWrapper stageConfigRepositoryWrapper;
    private final CodeMasterService codeMasterService;
    private final UserQueryService userQueryService;
    private final UserRoleService userRoleService;
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public StageConfigResponse getStageByKey(String key) {
        StageConfig stageConfig = stageConfigRepositoryWrapper.findByKeyWithException(key);
        
        StageConfig.StageConfigDetails stageConfigDetails = stageConfig.getStageConfig();
        List<StageConfig.PossibleNextStage> possibleNextStages = ValidationUtils.isNonNull(stageConfigDetails)
                && ValidationUtils.isNonNull(stageConfigDetails.getPossibleNextStages())
                ? stageConfigDetails.getPossibleNextStages()
                : Collections.emptyList();

        possibleNextStages = filterByCurrentUserRole(possibleNextStages);

        List<String> possibleNextStageKeys = possibleNextStages.stream()
                .map(StageConfig.PossibleNextStage::getStageKey)
                .collect(Collectors.toList());

        StageConfig.AssigneeRoles assigneeRolesEntity = stageConfig.getAssigneeRoles();
        List<String> assigneeRoles = ValidationUtils.isNonNull(assigneeRolesEntity)
                && ValidationUtils.isNonNull(assigneeRolesEntity.getRoles())
                ? assigneeRolesEntity.getRoles()
                : Collections.emptyList();

        List<CodeValueResponse> subStages = fetchSubStages(stageConfig.getSubStagesCode());

        return StageConfigResponse.from(stageConfig, possibleNextStageKeys, assigneeRoles, subStages);
    }

    private List<CodeValueResponse> fetchSubStages(String subStagesCode) {
        if (!ValidationUtils.isNonNullOrEmpty(subStagesCode)) {
            return Collections.emptyList();
        }
        
        try {
            List<CodeValueResponse> codeValues = codeMasterService.getAllCodeValuesByCodeKey(subStagesCode, true, "default");
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
            Set<String> allRoles = collectRolesFromStages(processedStageKeys);
            
            if (allRoles.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Get users for all roles based on provided office key (includes up and down hierarchy)
            List<UserAssignmentResponse> allUsers = userQueryService.getUsersByOfficeAndRoles(new ArrayList<>(allRoles), officeKey);
            
            // Deduplicate by username to avoid returning same user multiple times
            return deduplicateUsers(allUsers);
        } catch (Exception e) {
            log.error("Unexpected error in getAssignableUsersForStages: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<UserAssignmentResponse> getAssignableUsersForStagesByCurrentUser(List<String> stageKeys) {
        try {
            // Get current user's office
            String currentUserOfficeKey = getCurrentUserOfficeKey();
            if (!ValidationUtils.isNonNull(currentUserOfficeKey)) {
                return Collections.emptyList();
            }
            
            List<String> processedStageKeys = processStageKeys(stageKeys);
            if (processedStageKeys.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Collect all unique assignee roles from all stages
            Set<String> allRoles = collectRolesFromStages(processedStageKeys);
            
            if (allRoles.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Get users for all roles based on current user's office (down hierarchy only)
            List<UserAssignmentResponse> allUsers = userQueryService.getUsersByOfficeAndRolesDownHierarchy(
                    new ArrayList<>(allRoles), currentUserOfficeKey);
            
            // Deduplicate by username
            return deduplicateUsers(allUsers);
        } catch (Exception e) {
            log.error("Unexpected error in getAssignableUsersForStagesByCurrentUser: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    private String getCurrentUserOfficeKey() {
        try {
            String username = UserContext.getUsername();
            if (!ValidationUtils.isNonNull(username)) {
                return null;
            }
            
            Object user = getUserByUsername(username);
            if (user == null) {
                return null;
            }
            
            // Get user ID using reflection
            java.lang.reflect.Method getId = user.getClass().getMethod("getId");
            Long userId = (Long) getId.invoke(user);
            
            Optional<Object> staffOpt = findStaffByUserId(userId);
            if (staffOpt.isEmpty()) {
                return null;
            }
            
            return getStaffOfficeKey(staffOpt.get());
        } catch (Exception e) {
            log.warn("Failed to get current user's office key: {}", e.getMessage());
            return null;
        }
    }
    
    private Object getUserByUsername(String username) {
        try {
            Object userReadService = applicationContext.getBean("userReadServiceImpl");
            java.lang.reflect.Method getUserByUsername = userReadService.getClass()
                    .getMethod("getUserByUsername", String.class);
            return getUserByUsername.invoke(userReadService, username);
        } catch (Exception e) {
            log.warn("Failed to get user by username: {}", e.getMessage());
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private Optional<Object> findStaffByUserId(Long userId) {
        try {
            Object staffRepository = applicationContext.getBean("staffRepository");
            java.lang.reflect.Method findByUserId = staffRepository.getClass().getMethod("findByUserId", Long.class);
            Object result = findByUserId.invoke(staffRepository, userId);
            if (result instanceof Optional) {
                return (Optional<Object>) result;
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    private String getStaffOfficeKey(Object staff) {
        try {
            java.lang.reflect.Method getOfficeKey = staff.getClass().getMethod("getOfficeKey");
            return (String) getOfficeKey.invoke(staff);
        } catch (Exception e) {
            return null;
        }
    }
    
    private List<StageConfig.PossibleNextStage> filterByCurrentUserRole(List<StageConfig.PossibleNextStage> possibleNextStages) {
        if (possibleNextStages.isEmpty()) {
            return possibleNextStages;
        }

        String username = UserContext.getUsername();
        if (!ValidationUtils.isNonNull(username)) {
            return Collections.emptyList();
        }

        List<String> userRoles = userRoleService.getRolesByUsername(username);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        if (userRoles.contains(Role.ADMIN.name())) {
            return possibleNextStages;
        }

        return possibleNextStages.stream()
                .filter(stage -> stage.getAllowedRoles() == null
                        || stage.getAllowedRoles().isEmpty()
                        || stage.getAllowedRoles().stream().anyMatch(userRoles::contains))
                .collect(Collectors.toList());
    }

    private Set<String> collectRolesFromStages(List<String> stageKeys) {
        Set<String> allRoles = new HashSet<>();
        for (String stageKey : stageKeys) {
            try {
                StageConfigResponse stageConfig = getStageByKey(stageKey);
                if (stageConfig != null && !CollectionUtils.isEmpty(stageConfig.getAssigneeRoles())) {
                    allRoles.addAll(stageConfig.getAssigneeRoles());
                }
            } catch (Exception e) {
                log.debug("Failed to get stage config for key: {}, error: {}", stageKey, e.getMessage());
                continue;
            }
        }
        return allRoles;
    }
    
    private List<UserAssignmentResponse> deduplicateUsers(List<UserAssignmentResponse> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        
        Map<String, UserAssignmentResponse> uniqueUsers = users.stream()
                .collect(Collectors.toMap(
                        UserAssignmentResponse::getUsername,
                        user -> user,
                        (existing, replacement) -> existing
                ));
        
        return new ArrayList<>(uniqueUsers.values());
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
