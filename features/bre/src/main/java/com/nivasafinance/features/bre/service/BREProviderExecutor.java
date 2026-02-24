package com.nivasafinance.features.bre.service;

import com.nivasafinance.features.bre.entity.BREConfigs;

public interface BREProviderExecutor {
    String evaluate(BREConfigs config, String inputJson);
}
