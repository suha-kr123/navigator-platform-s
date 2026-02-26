package com.nivasafinance.features.bulkoperations.operations.stageassignment;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationProcessingProperties;
import com.nivasafinance.features.bulkoperations.common.dto.ProcessingResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.engine.BaseBulkOperationProcessor;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingPersistence;
import com.nivasafinance.features.bulkoperations.service.BulkOperationReportService;
import com.nivasafinance.features.bulkoperations.service.BulkOperationRowTransactionRunner;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class StageAssignmentProcessor extends BaseBulkOperationProcessor {

    private static final String STATUS = "status";
    private static final String ASSIGNED = "ASSIGNED";

    private final LeadStageHistoryWriteService leadStageHistoryWriteService;

    public StageAssignmentProcessor(
            BulkOperationReportService reportService,
            com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService fileStorageService,
            BulkOperationRepository bulkOperationRepository,
            BulkOperationProcessingPersistence persistence,
            BulkOperationRowTransactionRunner rowTransactionRunner,
            BulkOperationCsvProperties csvProperties,
            BulkOperationExceptionFactory exceptionFactory,
            BulkOperationProcessingProperties processingProperties,
            LeadStageHistoryWriteService leadStageHistoryWriteService) {
        super(reportService, fileStorageService, bulkOperationRepository, persistence, rowTransactionRunner, csvProperties, exceptionFactory, processingProperties.getBatchSize());
        this.leadStageHistoryWriteService = leadStageHistoryWriteService;
    }

    @Override
    public BulkOperationType getType() {
        return BulkOperationType.STAGE_ASSIGNMENT_CHANGE;
    }

    @Override
    public ProcessingResult processRow(BulkOperation bulkOperation, Map<String, Object> row) {
        UUID leadId = getUuid(row, StageAssignmentRowKeys.LEAD_IDENTIFIER);
        String stageKey = getString(row, StageAssignmentRowKeys.STAGE_KEY);
        String assignedTo = getString(row, StageAssignmentRowKeys.ASSIGNED_TO);

        Map<String, Object> original = Map.of(
                StageAssignmentRowKeys.LEAD_IDENTIFIER, leadId != null ? leadId.toString() : "",
                StageAssignmentRowKeys.STAGE_KEY, stageKey != null ? stageKey : "",
                StageAssignmentRowKeys.ASSIGNED_TO, assignedTo != null ? assignedTo : "");

        if (leadId == null || stageKey == null || stageKey.isBlank() || assignedTo == null || assignedTo.isBlank()) {
            return ProcessingResult.failed(StageAssignmentRowKeys.LEAD_IDENTIFIER + ", " + StageAssignmentRowKeys.STAGE_KEY + " and " + StageAssignmentRowKeys.ASSIGNED_TO + " are required", original);
        }

        if (bulkOperation.getStatus().isDryRunFlow()) {
            Map<String, Object> newValues = new HashMap<>(original);
            newValues.put(STATUS, ASSIGNED + " (dry-run)");
            return ProcessingResult.success(original, newValues);
        }

        try {
            leadStageHistoryWriteService.changeAssignment(leadId, stageKey, assignedTo);
            Map<String, Object> newValues = new HashMap<>(original);
            newValues.put(STATUS, ASSIGNED);
            return ProcessingResult.success(original, newValues);
        } catch (Exception e) {
            return ProcessingResult.failed(ExceptionUtils.getRootCauseMessage(e), original);
        }
    }

    @Override
    protected Map<String, String> toRowDataMap(Map<String, Object> row) {
        Map<String, String> m = new HashMap<>();
        put(m, row, StageAssignmentRowKeys.LEAD_IDENTIFIER);
        put(m, row, StageAssignmentRowKeys.STAGE_KEY);
        put(m, row, StageAssignmentRowKeys.ASSIGNED_TO);
        return m;
    }

    @Override
    protected String getRowReference(Map<String, Object> row) {
        UUID id = getUuid(row, StageAssignmentRowKeys.LEAD_IDENTIFIER);
        return id != null ? id.toString() : null;
    }

    private static void put(Map<String, String> target, Map<String, Object> source, String key) {
        Object v = source.get(key);
        if (v != null) target.put(key, v.toString());
    }

    private static String getString(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static UUID getUuid(Map<String, Object> row, String key) {
        String s = getString(row, key);
        if (s == null) return null;
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

