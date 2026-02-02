package com.nivasafinance.features.bulkoperations.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.MessageProvider;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.exception.ValidationException;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationProcessingProperties;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class BulkOperationProcessingListener {

    private static final String OPERATION_ID = "operationId";
    private static final String MESSAGE_ID = "messageId";
    private static final String DEFAULT_USER = "system";

    private static final String LOG_INIT = "BulkOperationProcessingListener initialized. Provider: {}";
    private static final String LOG_POLL_FAILED = "Failed to poll processing queue";
    private static final String LOG_VALIDATED_OPERATION_FAILED = "Failed to process VALIDATED operation {}";

    private final MessagingProperties messagingProperties;
    private final ObjectProvider<SqsClient> sqsClientProvider;
    private final BulkOperationRepository bulkOperationRepository;
    private final BulkOperationProcessingService processingService;
    private final BulkOperationExceptionFactory exceptionFactory;
    private final BulkOperationProcessingProperties processingProperties;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    private final AtomicBoolean polling = new AtomicBoolean(false);
    private Semaphore processingSemaphore;

    @PostConstruct
    public void init() {
        this.processingSemaphore = new Semaphore(processingProperties.getMaxConcurrentProcessing());
        log.info(LOG_INIT, messagingProperties.getProvider());
    }

    @Scheduled(fixedDelayString = "${messaging.sqs.poll-delay-ms:1000}")
    public void pollProcessingQueue() {
        if (!polling.compareAndSet(false, true))
            return;

        try {
            if (isSqsProvider()) {
                List<Message> messages = receiveProcessingMessages();
                polling.set(false); // release lock so next poll can start while we process
                String queueUrl = messagingProperties.getSqs().resolveQueueUrl(QueueType.BULK_OPERATION_PROCESSING);
                SqsClient sqsClient = sqsClientProvider.getIfAvailable();
                for (Message message : messages) {
                    boolean processed = processProcessingMessage(message.body());
                    if (processed && sqsClient != null)
                        deleteMessage(queueUrl, message, sqsClient);
                }
            } else {
                pollLocalDatabase();
            }
        } catch (Exception ex) {
            log.error(LOG_POLL_FAILED, ex);
        } finally {
            polling.set(false);
        }
    }

    private boolean isSqsProvider() {
        return ValidationUtils.equals(messagingProperties.getProvider(), MessageProvider.SQS);
    }

    /**
     * Receives messages from the processing queue. Caller must hold polling lock only during this call.
     */
    private List<Message> receiveProcessingMessages() {
        SqsClient sqsClient = sqsClientProvider.getIfAvailable();
        if (ValidationUtils.isEmpty(sqsClient))
            return List.of();
        String queueUrl = messagingProperties.getSqs().resolveQueueUrl(QueueType.BULK_OPERATION_PROCESSING);
        ReceiveMessageRequest request = buildReceiveMessageRequest(queueUrl);
        ReceiveMessageResponse response = sqsClient.receiveMessage(request);
        return response.messages();
    }

    private ReceiveMessageRequest buildReceiveMessageRequest(String queueUrl) {
        return ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .waitTimeSeconds(messagingProperties.getSqs().getWaitTimeSeconds())
                .maxNumberOfMessages(messagingProperties.getSqs().getMaxMessages())
                .build();
    }

    private void pollLocalDatabase() {
        List<BulkOperation> validatedOperations = bulkOperationRepository
                .findByStatusOrderByCreatedAtAsc(BulkOperationStatus.VALIDATED);

        for (BulkOperation operation : validatedOperations) {
            try {
                String messageBody = serializeOperationToMessage(operation);
                processProcessingMessage(messageBody);
            } catch (Exception ex) {
                log.error(LOG_VALIDATED_OPERATION_FAILED, operation.getOperationIdentifier(), ex);
            }
        }
    }

    private String serializeOperationToMessage(BulkOperation operation) throws Exception {
        Map<String, Object> messageMap = Map.of(OPERATION_ID, operation.getOperationIdentifier().toString());
        return objectMapper.writeValueAsString(messageMap);
    }

    private boolean processProcessingMessage(String rawMessage) {
        TransactionTemplate template = createTransactionTemplate();
        return Boolean.TRUE.equals(template.execute(status -> {
            try {
                UUID operationId = extractOperationId(rawMessage);
                if (operationId == null)
                    return false;

                BulkOperation bulkOperation = bulkOperationRepository.findByOperationIdentifier(operationId)
                        .orElse(null);
                if (shouldSkipProcessing(bulkOperation))
                    return true;

                return executeWithUserContext(bulkOperation);
            } catch (Exception ex) {
                log.error("Processing failed for bulk operation (transaction will roll back): {}", ex.getMessage(), ex);
                status.setRollbackOnly();
                return false;
            }
        }));
    }

    private TransactionTemplate createTransactionTemplate() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return template;
    }

    /**
     * Skip if operation is missing, already in progress, or in a terminal state.
     * When skipped, return true so the message is deleted and not redelivered.
     */
    private boolean shouldSkipProcessing(BulkOperation bulkOperation) {
        if (ValidationUtils.isEmpty(bulkOperation))
            return true;
        BulkOperationStatus status = bulkOperation.getStatus();
        if (status == BulkOperationStatus.VALIDATED)
            return false; // only state we should process
        // Skip terminal states and in-progress to avoid redundant work or re-processing
        return status.isTerminal() || status == BulkOperationStatus.PROCESSING_IN_PROGRESS;
    }

    private boolean executeWithUserContext(BulkOperation bulkOperation) {
        if (processingSemaphore == null) {
            return runProcessing(bulkOperation);
        }
        try {
            processingSemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for processing permit");
            return false;
        }
        try {
            return runProcessing(bulkOperation);
        } finally {
            processingSemaphore.release();
        }
    }

    private boolean runProcessing(BulkOperation bulkOperation) {
        setUserContext(bulkOperation);
        try {
            processingService.process(bulkOperation);
            return true;
        } catch (Exception ex) {
            processingService.recordProcessingFailure(bulkOperation.getOperationIdentifier(), ex);
            return false;
        } finally {
            UserContext.clear();
        }
    }

    private UUID extractOperationId(String rawMessage) throws ValidationException {
        Map<String, Object> messageMap = parseMessageBody(rawMessage);
        String idStr = getOperationIdFromMessageMap(messageMap);
        if (ValidationUtils.isNullOrEmpty(idStr))
            throw exceptionFactory.operationIdRequiredException();
        return parseUuid(idStr);
    }

    private Map<String, Object> parseMessageBody(String rawMessage) throws ValidationException {
        try {
            return objectMapper.readValue(rawMessage, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw exceptionFactory.operationIdRequiredException();
        }
    }

    private String getOperationIdFromMessageMap(Map<String, Object> messageMap) {
        Object value = messageMap.getOrDefault(OPERATION_ID, messageMap.get(MESSAGE_ID));
        return value != null ? value.toString() : null;
    }

    private UUID parseUuid(String idStr) throws ValidationException {
        try {
            return UUID.fromString(idStr);
        } catch (IllegalArgumentException ex) {
            throw exceptionFactory.operationIdRequiredException();
        }
    }

    private void setUserContext(BulkOperation bulkOperation) {
        String username = bulkOperation.getCreatedBy();
        UserContext.setUsername(ValidationUtils.isNullOrEmpty(username) ? DEFAULT_USER : username);
    }

    private void deleteMessage(String queueUrl, Message message, SqsClient sqsClient) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
    }
}
