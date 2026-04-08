package com.nivasafinance.features.bre.service;

import com.nivasafinance.features.bre.enums.BREProvider;
import com.nivasafinance.features.bre.service.impl.GoRulesBREProviderExecutor;
import com.nivasafinance.features.bre.service.impl.LocalBREProviderExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BREProviderExecutorFactoryTest {

    @Mock
    private GoRulesBREProviderExecutor goRulesExecutor;

    @Mock
    private LocalBREProviderExecutor localExecutor;

    @InjectMocks
    private BREProviderExecutorFactory factory;

    // ── getExecutor ──

    @Test
    void getExecutor_whenProviderIsGorules_returnsGoRulesExecutor() {
        BREProviderExecutor result = factory.getExecutor(BREProvider.GORULES);

        assertSame(goRulesExecutor, result,
                "GORULES provider should return the GoRules executor instance");
    }

    @Test
    void getExecutor_whenProviderIsLocal_returnsLocalExecutor() {
        BREProviderExecutor result = factory.getExecutor(BREProvider.LOCAL);

        assertSame(localExecutor, result,
                "LOCAL provider should return the Local executor instance");
    }

    @Test
    void getExecutor_whenProviderIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> factory.getExecutor(null),
                "Null provider should throw IllegalArgumentException");
    }
}
