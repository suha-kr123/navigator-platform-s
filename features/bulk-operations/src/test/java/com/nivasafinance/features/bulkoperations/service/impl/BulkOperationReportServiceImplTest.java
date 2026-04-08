package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationReportLayoutNotFoundException;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationReportLayout;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkOperationReportServiceImplTest {

    @Mock
    private BulkOperationFileStorageService fileStorageService;

    @Mock
    private BulkOperationExceptionFactory exceptionFactory;

    @Test
    void buildAndSaveUnifiedReport_layoutMissing_throws() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.ONHOLD)
                .status(BulkOperationStatus.VALIDATED)
                .build();
        Map<BulkOperationType, BulkOperationReportLayout> layouts = new HashMap<>();
        layouts.put(BulkOperationType.DROPOFF, mock(BulkOperationReportLayout.class));
        BulkOperationReportServiceImpl service = new BulkOperationReportServiceImpl(fileStorageService, layouts, exceptionFactory);
        when(exceptionFactory.bulkOperationReportLayoutNotFoundException(BulkOperationType.ONHOLD))
                .thenReturn(new BulkOperationReportLayoutNotFoundException("missing layout"));

        assertThrows(BulkOperationReportLayoutNotFoundException.class,
                () -> service.buildAndSaveUnifiedReport(op, List.of(), List.of(), List.of()));
    }

    @Test
    void buildAndSaveUnifiedReport_savesCsvAndReturnsKey() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.VALIDATED)
                .build();

        BulkOperationReportLayout layout = spy(new BulkOperationReportLayout() {
            @Override
            public BulkOperationType getType() {
                return BulkOperationType.DROPOFF;
            }

            @Override
            public List<String> getReportHeaders() {
                return List.of("h1", "h2");
            }

            @Override
            public List<String> buildReportRow(CsvReportRow row, String rowStatus) {
                return List.of(String.valueOf(row.getRowNumber()), rowStatus);
            }
        });

        when(fileStorageService.saveCsvContent(anyString(), eq(id), eq("bulk_operation_report.csv")))
                .thenReturn("s3/key");
        Map<BulkOperationType, BulkOperationReportLayout> layouts = Map.of(BulkOperationType.DROPOFF, layout);
        BulkOperationReportServiceImpl service = new BulkOperationReportServiceImpl(fileStorageService, layouts, exceptionFactory);

        CsvValidationError err = CsvValidationError.builder()
                .rowNumber(1)
                .errorCode("E")
                .errorMessage("bad")
                .rowReference("r1")
                .build();
        CsvReportRow row = CsvReportRow.builder()
                .rowNumber(2)
                .rowReference("r2")
                .rowData(Map.of("lead_identifier", "L1"))
                .build();

        String key = service.buildAndSaveUnifiedReport(op, List.of(err), List.of(row), List.of());

        assertEquals("s3/key", key);
        verify(fileStorageService).saveCsvContent(anyString(), eq(id), eq("bulk_operation_report.csv"));
    }
}
