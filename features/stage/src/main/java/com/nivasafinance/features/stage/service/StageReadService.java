package com.nivasafinance.features.stage.service;

import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.stage.dto.StageConfigResponse;
import com.nivasafinance.features.stage.dto.StageFilterResponse;
import com.nivasafinance.features.stage.dto.StageTemplateResponse;

import java.util.List;
import java.util.Map;

public interface StageReadService {

    StageConfigResponse getStageByKey(String key);
    
    StageTemplateResponse getStageTemplate(String stageKey);
    
    Map<String, StageTemplateResponse> getStageTemplates(List<String> stageKeys);
    
    /**
     * Get assignable users for stages based on provided office key (includes up and down hierarchy).
     * @param stageKeys List of stage keys
     * @param officeKey Office key to determine hierarchy
     * @return List of assignable users
     */
    List<UserAssignmentResponse> getAssignableUsersForStages(List<String> stageKeys, String officeKey);
    
    /**
     * Get assignable users for stages based on current user's office (down hierarchy only).
     * @param stageKeys List of stage keys
     * @return List of assignable users
     */
    List<UserAssignmentResponse> getAssignableUsersForStagesByCurrentUser(List<String> stageKeys);
    
    List<StageFilterResponse> getAllActiveStages();
}

