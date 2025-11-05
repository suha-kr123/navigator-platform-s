package com.nivasafinance.features.stage.service;

import com.nivasafinance.features.stage.dto.StageConfigResponse;

public interface StageReadService {

    StageConfigResponse getStageByKey(String key);
}

