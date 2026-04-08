package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.dto.BulkOperationTypeResponse;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkOperationTypeConfigServiceImplTest {

    @Mock
    private BulkOperationCsvProperties csvProperties;

    @InjectMocks
    private BulkOperationTypeConfigServiceImpl bulkOperationTypeConfigService;

    @Test
    void getAllOperationTypes_mapsEveryEnumWithLimits() {
        when(csvProperties.getMaxRows()).thenReturn(100);
        when(csvProperties.getMaxFileSize()).thenReturn(2048L);

        List<BulkOperationTypeResponse> result = bulkOperationTypeConfigService.getAllOperationTypes();

        assertEquals(BulkOperationType.values().length, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getOperationType() == BulkOperationType.DROPOFF));
        assertEquals(100, result.get(0).getUploadLimits().getMaxRows());
        assertEquals(2048L, result.get(0).getUploadLimits().getMaxFileSizeBytes());
    }
}
