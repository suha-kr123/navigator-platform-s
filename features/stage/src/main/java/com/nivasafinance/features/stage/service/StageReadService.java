package com.nivasafinance.features.stage.service;

import com.nivasafinance.features.stage.dto.StageConfigResponse;
import com.nivasafinance.features.stage.dto.StageFilterResponse;
import com.nivasafinance.features.stage.dto.StageTemplateResponse;

import java.util.List;

public interface StageReadService {

    StageConfigResponse getStageByKey(String key);
    
    StageTemplateResponse getStageTemplate(String stageKey);
    
    List<StageFilterResponse> getAllActiveStages();
}

