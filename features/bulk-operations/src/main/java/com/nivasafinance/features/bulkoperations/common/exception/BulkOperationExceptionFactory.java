package com.nivasafinance.features.bulkoperations.common.exception;

import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ValidationException;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;

@Component
public class BulkOperationExceptionFactory {

    private final MessageSource messageSource;

    public BulkOperationExceptionFactory(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public BulkOperationTypeConfigNotFoundException bulkOperationTypeConfigNotFoundException(
            BulkOperationType operationType) {

        return new BulkOperationTypeConfigNotFoundException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.type.config.not.found",
                new Object[]{operationType.name()},
                messageSource
            )
        );
    }

    public BulkOperationValidatorNotFoundException bulkOperationValidatorNotFoundException(BulkOperationType operationType) {
        return new BulkOperationValidatorNotFoundException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.validator.not.found",
                new Object[]{operationType.name()},
                messageSource
            )
        );
    }

    public BulkOperationProcessorNotFoundException bulkOperationProcessorNotFoundException(BulkOperationType operationType) {
        return new BulkOperationProcessorNotFoundException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.processor.not.found",
                new Object[]{operationType.name()},
                messageSource
            )
        );
    }

    public BulkOperationCsvValidationException bulkOperationCsvValidationHeaderRequiredException() {
        return new BulkOperationCsvValidationException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.csv.validation.header.required",
                null,
                messageSource
            )
        );
    }

    public BulkOperationCsvValidationException bulkOperationCsvValidationMissingColumnsException(List<String> missingColumns) {
        return new BulkOperationCsvValidationException(ExceptionUtils.createLocalizedMessage("error.bulk.operation.csv.validation.missing.columns", new Object[]{String.join(", ", missingColumns)}, messageSource));
    }

    public BulkOperationCsvValidationException bulkOperationCsvValidationFileSizeExceededException() {
        return new BulkOperationCsvValidationException(ExceptionUtils.createLocalizedMessage("error.bulk.operation.csv.validation.file.size.exceeded", null, messageSource));
    }

    public BulkOperationCsvValidationException bulkOperationCsvValidationException(String message) {
        return new BulkOperationCsvValidationException(ExceptionUtils.createLocalizedMessage(message, null, messageSource));
    }

    public BulkOperationCsvValidationException bulkOperationCsvValidationFileParseFailedException() {
        return new BulkOperationCsvValidationException(ExceptionUtils.createLocalizedMessage("error.bulk.operation.csv.validation.file.parse.failed", null, messageSource));
    }

    public BulkOperationCsvValidationException bulkOperationCsvValidationFileEmptyException() {
        return new BulkOperationCsvValidationException(ExceptionUtils.createLocalizedMessage("error.bulk.operation.csv.validation.file.empty", null, messageSource));
    }

    public BulkOperationCsvValidationException bulkOperationCsvValidationFileExtensionException() {
        return new BulkOperationCsvValidationException(ExceptionUtils.createLocalizedMessage("error.bulk.operation.csv.validation.file.extension", null, messageSource));
    }

    public BulkOperationCsvValidationException bulkOperationCsvValidationRowCountExceededException(int maxRows) {
        return new BulkOperationCsvValidationException(ExceptionUtils.createLocalizedMessage("error.bulk.operation.csv.validation.row.count.exceeded", new Object[]{maxRows}, messageSource));
    }

    public BulkOperationCsvValidationException bulkOperationCsvReportGenerationFailedException() {
        return new BulkOperationCsvValidationException(ExceptionUtils.createLocalizedMessage("error.bulk.operation.report.generation.failed", null, messageSource));
    }

    public ValidationException operationIdRequiredException() {
        return new ValidationException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.processing.operation.id.required",
                null,
                messageSource
            )
        );
    }

    public String createValidationFailedAfterRetriesMessage(int retryCount, int maxRetryCount, String detail) {
        return ExceptionUtils.createLocalizedMessage(
            "error.bulk.operation.validation.failed.after.retries",
            new Object[]{retryCount, maxRetryCount, detail},
            messageSource
        );
    }

    public String createProcessingFailedAfterRetriesMessage(int retryCount, int maxRetryCount, String detail) {
        return ExceptionUtils.createLocalizedMessage(
            "error.bulk.operation.processing.failed.after.retries",
            new Object[]{retryCount, maxRetryCount, detail},
            messageSource
        );
    }

    public BulkOperationNotFoundException bulkOperationNotFoundException(UUID operationId) {
        return new BulkOperationNotFoundException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.not.found",
                new Object[]{operationId.toString()},
                messageSource
            )
        );
    }

    public BulkOperationDuplicateUploadException duplicateUploadException() {
        return new BulkOperationDuplicateUploadException(
            ExceptionUtils.createLocalizedMessage("error.bulk.operation.duplicate.upload", null, messageSource)
        );
    }

    /**
     * Duplicate upload exception with time remaining until re-upload is allowed.
     *
     * @param minutesRemaining minutes until the same file can be re-uploaded (displayed to user, typically at least 1)
     */
    public BulkOperationDuplicateUploadException duplicateUploadException(int minutesRemaining) {
        String message = minutesRemaining == 1
                ? ExceptionUtils.createLocalizedMessage("error.bulk.operation.duplicate.upload.with.time.one", null, messageSource)
                : ExceptionUtils.createLocalizedMessage(
                        "error.bulk.operation.duplicate.upload.with.time",
                        new Object[]{minutesRemaining},
                        messageSource);
        return new BulkOperationDuplicateUploadException(message);
    }

    public BulkOperationNotCancellableException bulkOperationNotCancellableException(UUID operationId) {
        return new BulkOperationNotCancellableException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.not.cancellable",
                new Object[]{operationId.toString()},
                messageSource
            )
        );
    }

    public BulkOperationReportNotAvailableException bulkOperationReportNotAvailableException(UUID operationId) {
        return new BulkOperationReportNotAvailableException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.report.not.available",
                new Object[]{operationId.toString()},
                messageSource
            )
        );
    }

    public BulkOperationReportLayoutNotFoundException bulkOperationReportLayoutNotFoundException(BulkOperationType operationType) {
        return new BulkOperationReportLayoutNotFoundException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.report.layout.not.found",
                new Object[]{operationType.name()},
                messageSource
            )
        );
    }

    public BulkOperationDuplicateConfigurationException duplicateValidatorException(BulkOperationType operationType) {
        return new BulkOperationDuplicateConfigurationException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.duplicate.validator",
                new Object[]{operationType.name()},
                messageSource
            )
        );
    }

    public BulkOperationDuplicateConfigurationException duplicateProcessorException(BulkOperationType operationType) {
        return new BulkOperationDuplicateConfigurationException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.duplicate.processor",
                new Object[]{operationType.name()},
                messageSource
            )
        );
    }

    public BulkOperationDuplicateConfigurationException duplicateReportLayoutException(BulkOperationType operationType) {
        return new BulkOperationDuplicateConfigurationException(
            ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.duplicate.report.layout",
                new Object[]{operationType.name()},
                messageSource
            )
        );
    }

    public BulkOperationStorageException storageSaveFailedException(UUID operationId, Throwable cause) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.storage.save.failed",
                new Object[]{operationId != null ? operationId.toString() : "unknown"},
                messageSource
        );
        return new BulkOperationStorageException(message, cause);
    }

    public BulkOperationStorageException storageFetchFailedException(String storageKey, Throwable cause) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.storage.fetch.failed",
                new Object[]{storageKey != null ? storageKey : "unknown"},
                messageSource
        );
        return new BulkOperationStorageException(message, cause);
    }

    public BulkOperationStorageException storageSaveReportFailedException(UUID operationId, Throwable cause) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.bulk.operation.storage.save.report.failed",
                new Object[]{operationId != null ? operationId.toString() : "unknown"},
                messageSource
        );
        return new BulkOperationStorageException(message, cause);
    }

    public BulkOperationStorageException storageFileRequiredException() {
        return new BulkOperationStorageException(
                ExceptionUtils.createLocalizedMessage("error.bulk.operation.storage.file.required", null, messageSource)
        );
    }

    public BulkOperationStorageException storageKeyRequiredException() {
        return new BulkOperationStorageException(
                ExceptionUtils.createLocalizedMessage("error.bulk.operation.storage.key.required", null, messageSource)
        );
    }

    public BulkOperationStorageException storageCsvContentRequiredException() {
        return new BulkOperationStorageException(
                ExceptionUtils.createLocalizedMessage("error.bulk.operation.storage.csv.content.required", null, messageSource)
        );
    }

    public BulkOperationNotCompletedException dryRunOperationNotCompletedException(UUID operationId) {
        String message = ExceptionUtils.createLocalizedMessage(
            "error.bulk.operation.dry.run.not.completed",
            new Object[]{operationId.toString()},
            messageSource
        );
        return new BulkOperationNotCompletedException(message);
    }
}