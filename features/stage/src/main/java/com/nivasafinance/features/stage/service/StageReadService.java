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
    
    List<UserAssignmentResponse> getAssignableUsersForStages(List<String> stageKeys);
    
    List<StageFilterResponse> getAllActiveStages();
}

