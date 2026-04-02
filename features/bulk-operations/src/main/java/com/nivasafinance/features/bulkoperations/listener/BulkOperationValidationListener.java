package com.nivasafinance.features.bulkoperations.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.MessageProvider;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.common.exception.ValidationException;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationCsvValidationException;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationNotFoundException;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationValidatorNotFoundException;
import com.nivasafinance.features.bulkoperations.common.exception.InvalidBulkOperationTypeException;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingQueuePublisher;
import com.nivasafinance.features.bulkoperations.service.BulkValidationService;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import com.nivasafinance.features.bulkoperations.storage.StoredFileMultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BulkOperationValidationListener {

    private static final String OPERATION_ID = "operationId";
    private static final String MESSAGE_ID = "messageId";
    private static final String DEFAULT_USER = "system";
    private static final String FILE = "file";
    private static final String FILE_CONTENT_TYPE = "text/csv";

    private static final String LOG_INIT = "BulkOperationValidationListener initialized. Provider: {}, Poll delay: {}ms";
    private static final String LOG_POLL_FAILED = "Failed to poll BULK_OPERATION_VALIDATION queue";
    private static final String LOG_UPLOADED_OPERATION_FAILED = "Failed to process UPLOADED operation {}";
    private static final String LOG_LOAD_FILE_FAILED = "Failed to load file for bulk operation {}";
    private static final String LOG_PARSE_OPERATION_ID_FAILED = "Failed to parse operationId from message: {}";
    private static final String LOG_DELETE_MESSAGE_FAILED = "Failed to delete message from queue {}";
    private static final String LOG_UPDATE_VALIDATION_FAILURE_FAILED = "Cannot update validation failure: operation {} not found";
    private static final String LOG_FAILED_TO_POLL_SQS_VALIDATION_QUEUE = "Failed to poll SQS validation queue: {}";

    private static final int PROCESSING_TIMEOUT_HOURS = 24;

    @org.springframework.beans.factory.annotation.Value("${bulk.operations.validation-timeout-hours:2}")
    private int validationTimeoutHours;

    private final MessagingProperties messagingProperties;
    private final ObjectProvider<SqsClient> sqsClientProvider;
    private final BulkOperationRepository bulkOperationRepository;
    private final BulkValidationService validationService;
    private final BulkOperationFileStorageService fileStorageService;
    private final BulkOperationProcessingQueuePublisher processingQueuePublisher;
    private final BulkOperationExceptionFactory exceptionFactory;
    private final ObjectMapper objectMapper;

    private static final long ERROR_BACKOFF_MS = 5_000L;
    private volatile boolean running;
    private Thread pollerThread;

    @PostConstruct
    public void init() {
        log.info(LOG_INIT, messagingProperties.getProvider(), messagingProperties.getSqs().getPollDelayMs());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startPolling() {
        if (!isSqsProvider()) {
            return;
        }
        running = true;
        pollerThread = new Thread(this::pollSqsLoop, "bulk-validation-poller");
        pollerThread.setDaemon(true);
        pollerThread.start();
        log.info("BulkOperationValidationListener: SQS continuous poller started");
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        running = false;
        if (pollerThread != null) {
            pollerThread.interrupt();
        }
    }

    @Scheduled(fixedDelayString = "${messaging.sqs.poll-delay-ms:1000}")
    public void pollLocalFallback() {
        if (isSqsProvider()) {
            return;
        }
        try {
            pollLocalDatabase();
        } catch (Exception ex) {
            log.error(LOG_POLL_FAILED, ex);
        }
    }

    private void pollSqsLoop() {
        String queueUrl = messagingProperties.getSqs().resolveQueueUrl(QueueType.BULK_OPERATION_VALIDATION);

        while (running) {
            try {
                SqsClient sqsClient = sqsClientProvider.getIfAvailable();
                if (sqsClient == null) {
                    log.warn("BulkOperationValidationListener: SqsClient not available, retrying in {}ms", ERROR_BACKOFF_MS);
                    Thread.sleep(ERROR_BACKOFF_MS);
                    continue;
                }

                List<Message> messages = sqsClient.receiveMessage(buildReceiveMessageRequest(queueUrl)).messages();

                if (!messages.isEmpty()) {
                    log.info("BulkOperationValidationListener: received {} message(s) from validation queue", messages.size());
                }

                for (Message message : messages) {
                    boolean processed = processValidationMessage(message.body());
                    if (processed) {
                        deleteMessage(queueUrl, message, sqsClient);
                    }
                }
            } catch (Exception ex) {
                if (running) {
                    log.warn(LOG_FAILED_TO_POLL_SQS_VALIDATION_QUEUE, ex.getMessage());
                    try { Thread.sleep(ERROR_BACKOFF_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        log.info("BulkOperationValidationListener SQS poll loop stopped");
    }

    private boolean isSqsProvider() {
        return ValidationUtils.equals(messagingProperties.getProvider(), MessageProvider.SQS);
    }

    private ReceiveMessageRequest buildReceiveMessageRequest(String queueUrl) {
        return ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .waitTimeSeconds(messagingProperties.getSqs().getWaitTimeSeconds())
                .maxNumberOfMessages(messagingProperties.getSqs().getMaxMessages())
                .build();
    }

    private void pollLocalDatabase() {
        List<BulkOperation> operationsToValidate = bulkOperationRepository
                .findByStatusInOrderByCreatedAtAsc(List.of(
                        BulkOperationStatus.UPLOADED,
                        BulkOperationStatus.UPLOADED_DRY_RUN,
                        BulkOperationStatus.VALIDATION_IN_PROGRESS,
                        BulkOperationStatus.VALIDATION_IN_PROGRESS_DRY_RUN
                ));
        if (!operationsToValidate.isEmpty()) {
            log.info("BulkOperationValidationListener: DB poll found {} operation(s) to validate", operationsToValidate.size());
        }

        for (BulkOperation operation : operationsToValidate) {
            try {
                String messageBody = serializeOperationToMessage(operation);
                processValidationMessage(messageBody);
            } catch (Exception ex) {
                log.error(LOG_UPLOADED_OPERATION_FAILED, operation.getOperationIdentifier(), ex);
            }
        }
    }

    private String serializeOperationToMessage(BulkOperation operation) throws Exception {
        Map<String, Object> messageMap = Map.of(OPERATION_ID, operation.getOperationIdentifier().toString());
        return objectMapper.writeValueAsString(messageMap);
    }

    private boolean processValidationMessage(String rawMessage) {
        UUID operationId = extractOperationId(rawMessage);
        if (ValidationUtils.isEmpty(operationId)) {
            log.warn("BulkOperationValidationListener: no operationId in message, rawMessage={}", rawMessage);
            return false;
        }
        log.info("BulkOperationValidationListener: processing validation message for operationId={}", operationId);
        BulkOperation bulkOperation = bulkOperationRepository.findByOperationIdentifier(operationId).orElse(null);
        if (ValidationUtils.isEmpty(bulkOperation)) {
            log.info("BulkOperationValidationListener: operation {} not found, skipping", operationId);
            return true; // delete message – operation no longer exists
        }
        // Skip if already in a terminal state or already validated – avoid re-running validation and overwriting VALIDATION_FAILED
        BulkOperationStatus s = bulkOperation.getStatus();
        if (s != BulkOperationStatus.UPLOADED && s != BulkOperationStatus.UPLOADED_DRY_RUN
                && s != BulkOperationStatus.VALIDATION_IN_PROGRESS && s != BulkOperationStatus.VALIDATION_IN_PROGRESS_DRY_RUN) {
            log.info("BulkOperationValidationListener: skipping operation {} – status already {}", operationId, s);
            return true; // delete message – already handled
        }
        return executeValidationWithUserContext(bulkOperation);
    }

    private boolean executeValidationWithUserContext(BulkOperation bulkOperation) {
        log.info("BulkOperationValidationListener: executeValidation for operation {}", bulkOperation.getOperationIdentifier());
        setUserContext(bulkOperation);
        try {
            markValidationInProgressIfNeeded(bulkOperation);
            bulkOperation = bulkOperationRepository.findByOperationIdentifier(bulkOperation.getOperationIdentifier())
                    .orElse(bulkOperation);

            MultipartFile file = loadFileFromStorage(bulkOperation);
            if (ValidationUtils.isEmpty(file)) {
                log.warn("BulkOperationValidationListener: file load failed for operation {}, applying failure", bulkOperation.getOperationIdentifier());
                applyFileLoadFailure(bulkOperation);
                return true;
            }

            log.info("BulkOperationValidationListener: calling validateAndUpdateOperation for {}", bulkOperation.getOperationIdentifier());
            validationService.validateAndUpdateOperation(bulkOperation, file);
            bulkOperation = bulkOperationRepository.findByOperationIdentifier(bulkOperation.getOperationIdentifier())
                    .orElse(bulkOperation);

            log.info("BulkOperationValidationListener: validation done, calling publishToProcessingQueueIfValidated for {}", bulkOperation.getOperationIdentifier());
            publishToProcessingQueueIfValidated(bulkOperation);
            return true;
        } catch (Exception ex) {
            log.error("BulkOperationValidationListener: validation failed for operation {}", bulkOperation.getOperationIdentifier(), ex);
            handleValidationFailure(bulkOperation, ex);
            return false;
        } finally {
            UserContext.clear();
        }
    }

    private void markValidationInProgressIfNeeded(BulkOperation bulkOperation) {
        BulkOperationStatus s = bulkOperation.getStatus();
        if (s == BulkOperationStatus.UPLOADED) {
            bulkOperation.setStatus(BulkOperationStatus.VALIDATION_IN_PROGRESS);
            bulkOperation.setValidationStartedAt(LocalDateTime.now());
            bulkOperation.setTimeoutAt(LocalDateTime.now().plus(validationTimeoutHours, ChronoUnit.HOURS));
            bulkOperationRepository.save(bulkOperation);
        } else if (s == BulkOperationStatus.UPLOADED_DRY_RUN) {
            bulkOperation.setStatus(BulkOperationStatus.VALIDATION_IN_PROGRESS_DRY_RUN);
            bulkOperation.setValidationStartedAt(LocalDateTime.now());
            bulkOperation.setTimeoutAt(LocalDateTime.now().plus(validationTimeoutHours, ChronoUnit.HOURS));
            bulkOperationRepository.save(bulkOperation);
        }
    }

    private void applyFileLoadFailure(BulkOperation bulkOperation) {
        bulkOperation.setStatus(BulkOperationStatus.VALIDATION_FAILED);
        bulkOperation.setValidationCompletedAt(LocalDateTime.now());
        bulkOperationRepository.save(bulkOperation);
    }

    private void publishToProcessingQueueIfValidated(BulkOperation bulkOperation) {
        boolean hasValidRows = bulkOperation.getValidRows() != null && bulkOperation.getValidRows() > 0;
        boolean hasWorkingFile = ValidationUtils.isNonNullOrEmpty(bulkOperation.getWorkingFileStorageKey());
        BulkOperationStatus status = bulkOperation.getStatus();
        boolean readyForProcessing = (status == BulkOperationStatus.VALIDATED || status == BulkOperationStatus.VALIDATED_DRY_RUN);

        log.info(
                "Validation publish check for operation {}: status={}, validRows={}, workingFileKey={}, ready={}, provider={}",
                bulkOperation.getOperationIdentifier(),
                status,
                bulkOperation.getValidRows(),
                hasWorkingFile ? bulkOperation.getWorkingFileStorageKey() : "null",
                readyForProcessing,
                messagingProperties.getProvider());

        if (readyForProcessing && hasValidRows && hasWorkingFile) {
            bulkOperation.setTimeoutAt(LocalDateTime.now().plus(PROCESSING_TIMEOUT_HOURS, ChronoUnit.HOURS));
            bulkOperationRepository.save(bulkOperation);
            publishToProcessingQueue(bulkOperation.getOperationIdentifier());
            log.info("Published bulk operation {} to processing queue (validRows={})",
                    bulkOperation.getOperationIdentifier(), bulkOperation.getValidRows());
        } else {
            log.info("Skipping publish to processing queue for operation {}: status={}, validRows={}, hasWorkingFile={}",
                    bulkOperation.getOperationIdentifier(), bulkOperation.getStatus(),
                    bulkOperation.getValidRows(), hasWorkingFile);
        }
    }

    private void setUserContext(BulkOperation bulkOperation) {
        String username = bulkOperation.getCreatedBy();
        UserContext.setUsername(ValidationUtils.isNullOrEmpty(username) ? DEFAULT_USER : username);
    }

    private MultipartFile loadFileFromStorage(BulkOperation bulkOperation) {
        try {
            var inputStream = fileStorageService.fetchFile(bulkOperation.getFileStorageKey());
            byte[] content = inputStream.readAllBytes();
            inputStream.close();

            long fileSize = bulkOperation.getFileSize() != null
                    ? bulkOperation.getFileSize()
                    : content.length;

            return new StoredFileMultipartFile(
                    FILE,
                    bulkOperation.getFileName(),
                    FILE_CONTENT_TYPE,
                    fileSize,
                    content
            );
        } catch (Exception ex) {
            log.error(LOG_LOAD_FILE_FAILED, bulkOperation.getOperationIdentifier(), ex);
            return null;
        }
    }

    private void handleValidationFailure(BulkOperation bulkOperation, Exception ex) {
        UUID operationId = bulkOperation.getOperationIdentifier();
        BulkOperation fresh = bulkOperationRepository.findByOperationIdentifier(operationId).orElse(null);
        if (ValidationUtils.isEmpty(fresh)) {
            log.warn(LOG_UPDATE_VALIDATION_FAILURE_FAILED, operationId);
            return;
        }

        if (isValidationOrBusinessError(ex)) {
            fresh.setStatus(BulkOperationStatus.VALIDATION_FAILED);
            fresh.setErrorMessage(ExceptionUtils.getRootCauseMessage(ex));
            fresh.setValidationCompletedAt(LocalDateTime.now());
            bulkOperationRepository.save(fresh);
            return;
        }

        int retries = fresh.getRetryCount() != null ? fresh.getRetryCount() : 0;
        fresh.setRetryCount(retries + 1);
        fresh.setLastRetryAt(LocalDateTime.now());

        if (fresh.getRetryCount() >= fresh.getMaxRetryCount()) {
            fresh.setStatus(BulkOperationStatus.VALIDATION_FAILED);
            String errorMessage = exceptionFactory.createValidationFailedAfterRetriesMessage(
                    fresh.getRetryCount(),
                    fresh.getMaxRetryCount(),
                    ExceptionUtils.getRootCauseMessage(ex)
            );
            fresh.setErrorMessage(errorMessage);
            fresh.setValidationCompletedAt(LocalDateTime.now());
        }

        bulkOperationRepository.save(fresh);
    }

    private boolean isValidationOrBusinessError(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof BulkOperationCsvValidationException
                    || t instanceof ValidationException
                    || t instanceof InvalidBulkOperationTypeException
                    || t instanceof BulkOperationValidatorNotFoundException
                    || t instanceof BulkOperationNotFoundException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private void publishToProcessingQueue(UUID operationId) {
        // Defer to async so publish runs after validation flow completes,
        // avoiding any interference from scheduling/transaction context
        processingQueuePublisher.publishToProcessingQueueAsync(operationId);
    }

    private UUID extractOperationId(String rawMessage) {
        try {
            Map<String, Object> messageMap = parseMessageBody(rawMessage);
            String idStr = getOperationIdFromMessageMap(messageMap);
            if (ValidationUtils.isNullOrEmpty(idStr))
                return null;
            return parseUuid(idStr);
        } catch (Exception ex) {
            log.error(LOG_PARSE_OPERATION_ID_FAILED, rawMessage, ex);
            return null;
        }
    }

    private Map<String, Object> parseMessageBody(String rawMessage) throws Exception {
        return objectMapper.readValue(rawMessage, new TypeReference<>() {});
    }

    private String getOperationIdFromMessageMap(Map<String, Object> messageMap) {
        Object value = messageMap.getOrDefault(OPERATION_ID, messageMap.get(MESSAGE_ID));
        return value != null ? value.toString() : null;
    }

    private UUID parseUuid(String idStr) {
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void deleteMessage(String queueUrl, Message message, SqsClient sqsClient) {
        try {
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        } catch (Exception ex) {
            log.error(LOG_DELETE_MESSAGE_FAILED, queueUrl, ex);
        }
    }
}
