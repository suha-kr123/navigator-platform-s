package com.nivasafinance.notification.executor.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.MessageProvider;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.notification.orchestrator.service.NotificationReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Listener that polls the NOTIFICATION_EXECUTOR queue and executes notification receipts.
 * This is responsible for the execution phase:
 * 1. Receives receipt IDs from NOTIFICATION_EXECUTOR queue
 * 2. Loads the NotificationReceipt from database
 * 3. Executes the receipt (sends the notification via appropriate executor)
 * 4. Updates receipt status to COMPLETED or FAILED
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationExecutorListener {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final NotificationReceiptService notificationReceiptService;
    private final MessagingProperties messagingProperties;
    private final ObjectProvider<SqsClient> sqsClientProvider;
    private final PlatformTransactionManager transactionManager;
    private final com.nivasafinance.notification.orchestrator.repository.NotificationReceiptRepository notificationReceiptRepository;

    private static final long ERROR_BACKOFF_MS = 5_000L;

    private volatile boolean running;
    private Thread pollerThread;
    private ExecutorService executor;

    @PostConstruct
    public void start() {
        int poolSize = Math.max(1, messagingProperties.getSqs().getMaxMessages());
        AtomicInteger threadNumber = new AtomicInteger(0);
        executor = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r, "notification-executor-" + threadNumber.incrementAndGet());
            t.setDaemon(false);
            return t;
        });

        if (messagingProperties.getProvider() != MessageProvider.SQS) {
            log.info("NotificationExecutorListener: provider is not SQS, skipping continuous polling");
            return;
        }

        SqsClient sqsClient = sqsClientProvider.getIfAvailable();
        if (sqsClient == null) {
            log.warn("NotificationExecutorListener: SqsClient not available, skipping continuous polling");
            return;
        }

        running = true;
        pollerThread = new Thread(this::pollLoop, "notification-executor-poller");
        pollerThread.setDaemon(true);
        pollerThread.start();
        log.info("NotificationExecutorListener started. Pool size: {}", poolSize);
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (pollerThread != null) {
            pollerThread.interrupt();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void pollLoop() {
        String queueUrl = messagingProperties.getSqs().resolveQueueUrl(QueueType.NOTIFICATION_EXECUTOR);
        SqsClient sqsClient = sqsClientProvider.getIfAvailable();

        while (running && sqsClient != null) {
            try {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .waitTimeSeconds(messagingProperties.getSqs().getWaitTimeSeconds())
                        .maxNumberOfMessages(messagingProperties.getSqs().getMaxMessages())
                        .build();

                List<Message> messages = sqsClient.receiveMessage(request).messages();

                if (!messages.isEmpty()) {
                    log.info("Received {} message(s) from NOTIFICATION_EXECUTOR queue", messages.size());
                }

                List<CompletableFuture<Boolean>> futures = new ArrayList<>(messages.size());
                for (Message message : messages) {
                    String body = message.body();
                    CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            return processExecutorMessage(body);
                        } catch (Throwable t) {
                            log.error("Error processing executor message: {}", body, t);
                            return false;
                        }
                    }, executor);
                    futures.add(future);
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();

                for (int i = 0; i < messages.size(); i++) {
                    if (Boolean.TRUE.equals(futures.get(i).getNow(false))) {
                        deleteMessage(queueUrl, messages.get(i), sqsClient);
                    } else {
                        log.warn("Message not processed successfully, will remain in queue for retry: {}", messages.get(i).body());
                    }
                }
            } catch (Exception ex) {
                if (running) {
                    log.error("Error in NOTIFICATION_EXECUTOR queue poll loop", ex);
                    try { Thread.sleep(ERROR_BACKOFF_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        log.info("NotificationExecutorListener poll loop stopped");
    }

    /**
     * Processes a message from the NOTIFICATION_EXECUTOR queue.
     * The message contains a receiptId that should be executed.
     */
    private boolean processExecutorMessage(String rawMessage) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        // Parse receiptId outside the transaction so it's available in catch blocks
        UUID receiptId = extractReceiptId(rawMessage);
        if (receiptId == null) {
            log.error("Deleting malformed message from queue (no valid receiptId): {}", rawMessage);
            return true;
        }

        return Boolean.TRUE.equals(template.execute(status -> {
            try {
                log.info("Processing notification executor message: {}", rawMessage);

                var receiptOpt = notificationReceiptService.findById(receiptId);
                if (receiptOpt.isEmpty()) {
                    log.warn("Notification receipt not found: {}. This may be an old message or the receipt was deleted. Deleting message from queue.", receiptId);
                    return true;
                }

                var receipt = receiptOpt.get();
                if (receipt.getStatus() == NotificationStatus.COMPLETED) {
                    log.info("Receipt {} is already COMPLETED, skipping execution. Deleting message from queue.", receiptId);
                    return true;
                }

                if (receipt.getStatus() == NotificationStatus.FAILED) {
                    log.warn("Receipt {} is already FAILED, skipping execution. Deleting message from queue to prevent infinite retries.", receiptId);
                    return true;
                }

                log.info("Executing receipt {} (current status: {})", receiptId, receipt.getStatus());
                notificationReceiptService.executeReceipt(receiptId);
                log.info("Successfully executed receipt {}", receiptId);
                return true;
            } catch (Exception ex) {
                log.error("Failed to execute receipt from message: {}", rawMessage, ex);
                status.setRollbackOnly();
                try {
                    Map<String, Object> errorJson = new HashMap<>();
                    errorJson.put("message", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
                    errorJson.put("exceptionType", ex.getClass().getSimpleName());
                    notificationReceiptService.markReceiptFailedInNewTransaction(receiptId, errorJson);
                    log.warn("Marked receipt {} as FAILED. Deleting message from queue.", receiptId);
                    return true;
                } catch (Exception markEx) {
                    log.error("Failed to mark receipt {} as FAILED. Message will be retried by SQS.", receiptId, markEx);
                    return false;
                }
            }
        }));
    }

    /**
     * Extracts the receiptId from the raw SQS message body.
     * Returns null if the message is malformed or missing receiptId.
     */
    private UUID extractReceiptId(String rawMessage) {
        try {
            Map<String, Object> messageMap = parseMessage(rawMessage);
            String receiptIdStr = (String) messageMap.get("receiptId");
            if (receiptIdStr == null) {
                receiptIdStr = (String) messageMap.get("messageId");
            }
            if (receiptIdStr == null) {
                log.error("Executor message missing receiptId/messageId. Message: {}", rawMessage);
                return null;
            }
            return UUID.fromString(receiptIdStr);
        } catch (Exception ex) {
            log.error("Failed to extract receiptId from executor message: {}", rawMessage, ex);
            return null;
        }
    }

    /**
     * Marks a receipt as FAILED in a REQUIRES_NEW transaction so the error persists
     * even when the outer transaction rolls back.
     *
     * This fixes the Spring proxy self-invocation issue where
     * NotificationReceiptService.markReceiptFailed(REQUIRES_NEW) was called from
     * executeReceipt() within the same bean, causing REQUIRES_NEW to be ignored.
     */
    private void markReceiptFailedInNewTransaction(UUID receiptId, Exception ex) {
        if (receiptId == null) {
            return;
        }
        try {
            TransactionTemplate requiresNew = new TransactionTemplate(transactionManager);
            requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            requiresNew.execute(s -> {
                var receiptOpt = notificationReceiptRepository.findById(receiptId);
                if (receiptOpt.isEmpty()) {
                    log.warn("Cannot mark receipt {} as FAILED - receipt not found", receiptId);
                    return null;
                }
                var receipt = receiptOpt.get();
                receipt.setStatus(NotificationStatus.FAILED);

                Map<String, Object> errorJson = new java.util.HashMap<>();
                String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                errorJson.put("message", message);
                errorJson.put("exceptionType", ex.getClass().getSimpleName());
                // Include root cause if available
                if (ex.getCause() != null) {
                    errorJson.put("rootCause", ex.getCause().getMessage());
                }
                receipt.setErrorJson(errorJson);
                receipt.setUpdatedBy("system");

                Map<String, Object> remarks = new java.util.HashMap<>();
                remarks.put("error", message);
                remarks.put("timestamp", System.currentTimeMillis());
                receipt.setRemarks(remarks);

                notificationReceiptRepository.save(receipt);
                notificationReceiptRepository.flush();
                log.info("Receipt {} marked as FAILED in new transaction. Error: {}", receiptId, message);
                return null;
            });
        } catch (Exception updateEx) {
            log.error("Failed to mark receipt {} as FAILED in new transaction; receipt may stay INITIATED.", receiptId, updateEx);
        }
    }

    private Map<String, Object> parseMessage(String rawMessage) {
        try {
            return objectMapper.readValue(rawMessage, MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse SQS message body", ex);
        }
    }

    private void deleteMessage(String queueUrl, Message message, SqsClient sqsClient) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
    }
}