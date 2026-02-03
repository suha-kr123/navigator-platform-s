package com.nivasafinance.features.bulkoperations.operations.substagechange;

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
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Processor for SUBSTAGE_CHANGE bulk operations. Calls LeadStageHistoryWriteService.changeSubStage for each row.
 * Dry-run skips actual domain calls and returns simulated success.
 */
@Component
public class SubstageChangeProcessor extends BaseBulkOperationProcessor {

    private static final String STATUS = "status";
    private static final String SUBSTAGE_CHANGED = "SUBSTAGE_CHANGED";

    private final LeadStageHistoryWriteService leadStageHistoryWriteService;

    public SubstageChangeProcessor(
            BulkOperationReportService reportService,
            BulkOperationFileStorageService fileStorageService,
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
        return BulkOperationType.SUBSTAGE_CHANGE;
    }

    @Override
    public ProcessingResult processRow(BulkOperation bulkOperation, Map<String, Object> row) {
        UUID leadId = getUuid(row, SubstageChangeRowKeys.LEAD_IDENTIFIER);
        String stageKey = getString(row, SubstageChangeRowKeys.STAGE_KEY);
        String substageKey = getString(row, SubstageChangeRowKeys.SUBSTAGE_KEY);

        Map<String, Object> original = Map.of(
                SubstageChangeRowKeys.LEAD_IDENTIFIER, leadId != null ? leadId.toString() : "",
                SubstageChangeRowKeys.STAGE_KEY, stageKey != null ? stageKey : "",
                SubstageChangeRowKeys.SUBSTAGE_KEY, substageKey != null ? substageKey : "");

        if (leadId == null || stageKey == null || stageKey.isBlank() || substageKey == null || substageKey.isBlank()) {
            return ProcessingResult.failed(SubstageChangeRowKeys.LEAD_IDENTIFIER + ", " + SubstageChangeRowKeys.STAGE_KEY + " and " + SubstageChangeRowKeys.SUBSTAGE_KEY + " are required", original);
        }

        if (bulkOperation.getStatus().isDryRunFlow()) {
            Map<String, Object> newValues = new HashMap<>(original);
            newValues.put(STATUS, SUBSTAGE_CHANGED + " (dry-run)");
            return ProcessingResult.success(original, newValues);
        }

        try {
            leadStageHistoryWriteService.changeSubStage(leadId, stageKey, substageKey);
            Map<String, Object> newValues = new HashMap<>(original);
            newValues.put(STATUS, SUBSTAGE_CHANGED);
            return ProcessingResult.success(original, newValues);
        } catch (Exception e) {
            return ProcessingResult.failed(ExceptionUtils.getRootCauseMessage(e), original);
        }
    }

    @Override
    protected Map<String, String> toRowDataMap(Map<String, Object> row) {
        Map<String, String> m = new HashMap<>();
        put(m, row, SubstageChangeRowKeys.LEAD_IDENTIFIER);
        put(m, row, SubstageChangeRowKeys.STAGE_KEY);
        put(m, row, SubstageChangeRowKeys.SUBSTAGE_KEY);
        return m;
    }

    @Override
    protected String getRowReference(Map<String, Object> row) {
        UUID id = getUuid(row, SubstageChangeRowKeys.LEAD_IDENTIFIER);
        return id != null ? id.toString() : null;
    }

    private static void put(Map<String, String> target, Map<String, Object> source, String key) {
        Object v = source.get(key);
        if (v != null) target.put(key, v.toString());
    }

    private static String getString(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null || "".equals(v)) return null;
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
