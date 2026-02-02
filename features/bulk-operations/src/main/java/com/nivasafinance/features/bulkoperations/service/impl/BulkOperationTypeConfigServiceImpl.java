package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.dto.BulkOperationTypeResponse;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.service.BulkOperationTypeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BulkOperationTypeConfigServiceImpl implements BulkOperationTypeConfigService {

    private final BulkOperationCsvProperties csvProperties;

    @Override
    @Transactional(readOnly = true)
    public List<BulkOperationTypeResponse> getAllOperationTypes() {
        int maxRows = csvProperties.getMaxRows();
        long maxFileSizeBytes = csvProperties.getMaxFileSize();
        return Arrays.stream(BulkOperationType.values())
                .map(type -> BulkOperationTypeResponse.from(type, maxRows, maxFileSizeBytes))
                .collect(Collectors.toList());
    }
}
