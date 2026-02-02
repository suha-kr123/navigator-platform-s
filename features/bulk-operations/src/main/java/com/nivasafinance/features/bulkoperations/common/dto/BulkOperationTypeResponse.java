package com.nivasafinance.features.bulkoperations.common.dto;

import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkOperationTypeResponse {
    private BulkOperationType operationType;
    private String displayName;
    private String description;
    private List<String> requiredColumns;
    private List<String> optionalColumns;
    /** Upload limits (max rows, max file size) so clients can validate before upload. */
    private UploadLimits uploadLimits;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadLimits {
        private int maxRows;
        private long maxFileSizeBytes;
    }

    public static BulkOperationTypeResponse from(BulkOperationType type) {
        return BulkOperationTypeResponse.builder()
                .operationType(type)
                .displayName(type.getDisplayName())
                .description(type.getDescription())
                .requiredColumns(type.getRequiredColumns())
                .optionalColumns(type.getOptionalColumns())
                .build();
    }

    public static BulkOperationTypeResponse from(BulkOperationType type, int maxRows, long maxFileSizeBytes) {
        return BulkOperationTypeResponse.builder()
                .operationType(type)
                .displayName(type.getDisplayName())
                .description(type.getDescription())
                .requiredColumns(type.getRequiredColumns())
                .optionalColumns(type.getOptionalColumns())
                .uploadLimits(UploadLimits.builder()
                        .maxRows(maxRows)
                        .maxFileSizeBytes(maxFileSizeBytes)
                        .build())
                .build();
    }
}
