package com.nivasafinance.features.bre.service;

import com.nivasafinance.features.bre.enums.BREProvider;
import com.nivasafinance.features.bre.service.impl.GoRulesBREProviderExecutor;
import com.nivasafinance.features.bre.service.impl.LocalBREProviderExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BREProviderExecutorFactory {

    private final GoRulesBREProviderExecutor goRulesExecutor;
    private final LocalBREProviderExecutor localExecutor;

    public BREProviderExecutor getExecutor(BREProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("BREProvider must not be null");
        }
        return switch (provider) {
            case GORULES -> goRulesExecutor;
            case LOCAL -> localExecutor;
        };
    }
}
