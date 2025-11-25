package com.nivasafinance.features.stage.service;

import com.nivasafinance.features.stage.dto.StageConfigResponse;
import com.nivasafinance.features.stage.dto.StageTemplateResponse;

public interface StageReadService {

    StageConfigResponse getStageByKey(String key);
    
    StageTemplateResponse getStageTemplate(String stageKey);
}

