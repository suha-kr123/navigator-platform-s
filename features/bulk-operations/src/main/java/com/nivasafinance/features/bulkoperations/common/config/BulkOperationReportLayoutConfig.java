package com.nivasafinance.features.bulkoperations.common.config;

import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationReportLayout;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Wires report layouts by operation type. Layouts are auto-discovered from operation modules;
 * each operation provides its own {@link BulkOperationReportLayout} {@link org.springframework.stereotype.Component}.
 */
@Configuration
public class BulkOperationReportLayoutConfig {

    @Bean
    public Map<BulkOperationType, BulkOperationReportLayout> bulkOperationReportLayouts(
            List<BulkOperationReportLayout> layouts,
            BulkOperationExceptionFactory exceptionFactory) {
        return layouts.stream()
                .collect(Collectors.toMap(BulkOperationReportLayout::getType, Function.identity(),
                        (a, b) -> {
                            throw exceptionFactory.duplicateReportLayoutException(a.getType());
                        }));
    }
}
