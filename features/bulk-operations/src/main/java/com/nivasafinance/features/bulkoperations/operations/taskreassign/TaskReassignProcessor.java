package com.nivasafinance.features.bulkoperations.operations.taskreassign;

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
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.service.TaskWriteService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TaskReassignProcessor extends BaseBulkOperationProcessor {

    private static final String STATUS = "status";
    private static final String REASSIGNED = "REASSIGNED";

    private final TaskWriteService taskWriteService;

    public TaskReassignProcessor(
            BulkOperationReportService reportService,
            com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService fileStorageService,
            BulkOperationRepository bulkOperationRepository,
            BulkOperationProcessingPersistence persistence,
            BulkOperationRowTransactionRunner rowTransactionRunner,
            BulkOperationCsvProperties csvProperties,
            BulkOperationExceptionFactory exceptionFactory,
            BulkOperationProcessingProperties processingProperties,
            TaskWriteService taskWriteService) {
        super(reportService, fileStorageService, bulkOperationRepository, persistence, rowTransactionRunner, csvProperties, exceptionFactory, processingProperties.getBatchSize());
        this.taskWriteService = taskWriteService;
    }

    @Override
    public BulkOperationType getType() {
        return BulkOperationType.TASK_REASSIGN;
    }

    @Override
    public ProcessingResult processRow(BulkOperation bulkOperation, Map<String, Object> row) {
        UUID taskId = getUuid(row, TaskReassignRowKeys.TASK_IDENTIFIER);
        String assignedTo = getString(row, TaskReassignRowKeys.ASSIGNED_TO);

        Map<String, Object> original = Map.of(
                TaskReassignRowKeys.TASK_IDENTIFIER, taskId != null ? taskId.toString() : "",
                TaskReassignRowKeys.ASSIGNED_TO, assignedTo != null ? assignedTo : "");

        if (taskId == null || assignedTo == null || assignedTo.isBlank()) {
            return ProcessingResult.failed(TaskReassignRowKeys.TASK_IDENTIFIER + " and " + TaskReassignRowKeys.ASSIGNED_TO + " are required", original);
        }

        if (bulkOperation.getStatus().isDryRunFlow()) {
            Map<String, Object> newValues = new HashMap<>(original);
            newValues.put(STATUS, REASSIGNED + " (dry-run)");
            return ProcessingResult.success(original, newValues);
        }

        try {
            ReassignTaskRequest request = ReassignTaskRequest.builder()
                    .taskIdentifier(taskId)
                    .newAssignedTo(assignedTo)
                    .build();
            taskWriteService.reassignTask(request);
            Map<String, Object> newValues = new HashMap<>(original);
            newValues.put(STATUS, REASSIGNED);
            return ProcessingResult.success(original, newValues);
        } catch (Exception e) {
            return ProcessingResult.failed(ExceptionUtils.getRootCauseMessage(e), original);
        }
    }

    @Override
    protected Map<String, String> toRowDataMap(Map<String, Object> row) {
        Map<String, String> m = new HashMap<>();
        put(m, row, TaskReassignRowKeys.TASK_IDENTIFIER);
        put(m, row, TaskReassignRowKeys.ASSIGNED_TO);
        return m;
    }

    @Override
    protected String getRowReference(Map<String, Object> row) {
        UUID id = getUuid(row, TaskReassignRowKeys.TASK_IDENTIFIER);
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

