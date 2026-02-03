package com.nivasafinance.features.bulkoperations.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BulkOperationResponse {
    private UUID operationId;
    private BulkOperationType operationType;
    private BulkOperationStatus status;
    private Statistics statistics;
    private Progress progress;
    private Timestamps timestamps;
    private String createdBy;
    private Boolean canCancel;
    private String errorMessage;
    /** True when the summary report CSV is available for download; false if report generation failed or not yet generated. */
    private Boolean reportAvailable;
    private Map<String, Object> metadata;
    private ValidationSummary validationSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Statistics {
        private Integer totalRows;
        private Integer validRows;
        private Integer invalidRows;
        private Integer processedRows;
        private Integer successfulRows;
        private Integer failedRows;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationSummary {
        private Integer invalidRowCount;
        private List<Map<String, Object>> errorSummary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Progress {
        private Integer currentBatch;
        private Integer totalBatches;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Timestamps {
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;
        private LocalDateTime validationStartedAt;
        private LocalDateTime validationCompletedAt;
        private LocalDateTime processingStartedAt;
        private LocalDateTime processingCompletedAt;
        private LocalDateTime cancelledAt;
    }

    public static BulkOperationResponse from(BulkOperation bulkOperation) {
        return BulkOperationResponse.builder()
                .operationId(bulkOperation.getOperationIdentifier())
                .operationType(bulkOperation.getOperationType())
                .status(bulkOperation.getStatus())
                .statistics(Statistics.builder()
                        .totalRows(bulkOperation.getTotalRows())
                        .validRows(bulkOperation.getValidRows())
                        .invalidRows(bulkOperation.getInvalidRows())
                        .processedRows(bulkOperation.getProcessedRows())
                        .successfulRows(bulkOperation.getSuccessfulRows())
                        .failedRows(bulkOperation.getFailedRows())
                        .build())
                .progress(Progress.builder()
                        .currentBatch(bulkOperation.getCurrentBatch())
                        .totalBatches(bulkOperation.getTotalBatches())
                        .build())
                .timestamps(Timestamps.builder()
                        .createdAt(bulkOperation.getCreatedAt())
                        .updatedAt(bulkOperation.getUpdatedAt())
                        .createdBy(bulkOperation.getCreatedBy())
                        .updatedBy(bulkOperation.getUpdatedBy())
                        .validationStartedAt(bulkOperation.getValidationStartedAt())
                        .validationCompletedAt(bulkOperation.getValidationCompletedAt())
                        .processingStartedAt(bulkOperation.getProcessingStartedAt())
                        .processingCompletedAt(bulkOperation.getProcessingCompletedAt())
                        .cancelledAt(bulkOperation.getCancelledAt())
                        .build())
                .createdBy(bulkOperation.getCreatedBy())
                .canCancel(bulkOperation.getStatus().canCancel())
                .errorMessage(bulkOperation.getErrorMessage())
                .reportAvailable(bulkOperation.getSummaryStorageKey() != null)
                .metadata(bulkOperation.getMetaData())
                .validationSummary(buildValidationSummary(bulkOperation))
                .build();
    }

    private static ValidationSummary buildValidationSummary(BulkOperation bulkOperation) {
        if (bulkOperation.getInvalidRows() == null) {
            return null;
        }
        return ValidationSummary.builder()
                .invalidRowCount(bulkOperation.getInvalidRows())
                .errorSummary(List.of())
                .build();
    }
}
