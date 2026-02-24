package com.nivasafinance.features.bre.service.impl;

import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.service.BREProviderExecutor;
import org.springframework.stereotype.Component;

@Component
public class LocalBREProviderExecutor implements BREProviderExecutor {

    @Override
    public String evaluate(BREConfigs config, String inputJson) {
        throw new UnsupportedOperationException("LOCAL BRE provider is not implemented");
    }
}
