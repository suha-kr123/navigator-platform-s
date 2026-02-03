package com.nivasafinance.features.bulkoperations.common.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "n_bulk_operation")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class BulkOperation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "operation_identifier", nullable = false, unique = true, updatable = false)
    private UUID operationIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 50)
    private BulkOperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private BulkOperationStatus status;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "file_storage_key", length = 500)
    private String fileStorageKey;

    @Column(name = "total_rows")
    private Integer totalRows;

    @Column(name = "valid_rows")
    private Integer validRows;

    @Column(name = "invalid_rows")
    private Integer invalidRows;
    
    @Column(name = "processed_rows")
    @Builder.Default
    private Integer processedRows = 0;

    @Column(name = "successful_rows")
    @Builder.Default
    private Integer successfulRows = 0;

    @Column(name = "failed_rows")
    @Builder.Default
    private Integer failedRows = 0;

    @Column(name = "current_batch")
    @Builder.Default
    private Integer currentBatch = 0;

    @Column(name = "total_batches")
    @Builder.Default
    private Integer totalBatches = 0;

    @Column(name = "summary_storage_key", length = 500)
    private String summaryStorageKey;

    @Column(name = "validation_started_at")
    private LocalDateTime validationStartedAt;

    @Column(name = "validation_completed_at")
    private LocalDateTime validationCompletedAt;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retry_count")
    @Builder.Default
    private Integer maxRetryCount = 3;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @Column(name = "timeout_at")
    private LocalDateTime timeoutAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_data", columnDefinition = "jsonb")
    private Map<String, Object> metaData;

    @Column(name = "working_file_storage_key", length = 500)
    private String workingFileStorageKey;

    /** Storage key for validation errors JSON file (object storage); used when building unified report, then file can be deleted. */
    @Column(name = "validation_errors_storage_key", length = 500)
    private String validationErrorsStorageKey;

    @PrePersist
    void prePersist() {
        if (operationIdentifier == null) {
            operationIdentifier = UUID.randomUUID();
        }
    }

}
