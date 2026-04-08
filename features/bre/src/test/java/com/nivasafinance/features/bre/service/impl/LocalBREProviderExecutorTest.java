package com.nivasafinance.features.bre.service.impl;

import com.nivasafinance.features.bre.entity.BREConfigs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LocalBREProviderExecutorTest {

    @InjectMocks
    private LocalBREProviderExecutor executor;

    // ── evaluate ──

    @Test
    void evaluate_always_throwsUnsupportedOperationException() {
        BREConfigs config = BREConfigs.builder().uname("cfg").build();

        assertThrows(UnsupportedOperationException.class,
                () -> executor.evaluate(config, "{}"),
                "LOCAL provider should throw UnsupportedOperationException as it is not implemented");
    }
}
