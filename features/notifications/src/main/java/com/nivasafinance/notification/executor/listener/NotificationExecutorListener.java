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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private final AtomicBoolean executorPolling = new AtomicBoolean(false);
    private final AtomicInteger pollCount = new AtomicInteger(0);
    private ExecutorService executor;

    @PostConstruct
    public void init() {
        int poolSize = Math.max(1, messagingProperties.getSqs().getMaxMessages());
        AtomicInteger threadNumber = new AtomicInteger(0);
        executor = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r, "notification-executor-" + threadNumber.incrementAndGet());
            t.setDaemon(false);
            return t;
        });
        log.info("NotificationExecutorListener executor pool size: {}", poolSize);
        log.info("NotificationExecutorListener initialized. Provider: {}, SqsClient available: {}",
                messagingProperties.getProvider(),
                sqsClientProvider.getIfAvailable() != null);
        if (messagingProperties.getProvider() == MessageProvider.SQS) {
            try {
                String queueUrl = messagingProperties.getSqs().resolveQueueUrl(QueueType.NOTIFICATION_EXECUTOR);
                log.info("NOTIFICATION_EXECUTOR queue URL: {}", queueUrl);
                log.info("Poll delay: {}ms, Wait time: {}s, Max messages: {}",
                        messagingProperties.getSqs().getPollDelayMs(),
                        messagingProperties.getSqs().getWaitTimeSeconds(),
                        messagingProperties.getSqs().getMaxMessages());
            } catch (Exception ex) {
                log.error("Failed to resolve NOTIFICATION_EXECUTOR queue URL", ex);
            }
        } else {
            log.warn("NotificationExecutorListener will not poll - provider is not SQS: {}", messagingProperties.getProvider());
        }
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * Polls the NOTIFICATION_EXECUTOR queue and processes receipts.
     * This listener picks up receipts that are ready to be sent and executes them.
     */
    @Scheduled(fixedDelayString = "${messaging.sqs.poll-delay-ms:1000}")
    public void pollExecutorQueue() {
        int count = pollCount.incrementAndGet();

        if (count % 30 == 0) {
            log.info("NotificationExecutorListener polling (attempt #{}). Provider: {}, SqsClient available: {}",
                    count,
                    messagingProperties.getProvider(),
                    sqsClientProvider.getIfAvailable() != null);
        }

        // Only poll SQS if provider is SQS and SqsClient is available
        if (messagingProperties.getProvider() != MessageProvider.SQS) {
            if (count % 30 == 0) {
                log.warn("Skipping poll - provider is not SQS: {}", messagingProperties.getProvider());
            }
            return;
        }

        SqsClient sqsClient = sqsClientProvider.getIfAvailable();
        if (sqsClient == null) {
            if (count % 30 == 0) {
                log.warn("Skipping poll - SqsClient is not available");
            }
            return;
        }

        if (!executorPolling.compareAndSet(false, true)) {
            if (count % 30 == 0) {
                log.warn("Skipping poll - previous poll still in progress");
            }
            return;
        }

        try {
            String queueUrl = messagingProperties.getSqs().resolveQueueUrl(QueueType.NOTIFICATION_EXECUTOR);
            int waitTime = messagingProperties.getSqs().getWaitTimeSeconds();
            int maxMessages = messagingProperties.getSqs().getMaxMessages();

            if (count % 30 == 0) {
                log.info("Polling NOTIFICATION_EXECUTOR queue: {} (waitTime: {}s, maxMessages: {})",
                        queueUrl, waitTime, maxMessages);
            }

            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .waitTimeSeconds(waitTime)
                    .maxNumberOfMessages(maxMessages)
                    .build();

            log.debug("Calling receiveMessage on NOTIFICATION_EXECUTOR queue (poll attempt #{}, waitTime: {}s)",
                    count, waitTime);
            ReceiveMessageResponse response;
            try {
                response = sqsClient.receiveMessage(request);
            } catch (Exception receiveEx) {
                log.error("Exception during receiveMessage call on NOTIFICATION_EXECUTOR queue (poll attempt #{})",
                        count, receiveEx);
                throw receiveEx;
            }

            int messageCount = response.messages().size();

            if (messageCount > 0) {
                log.info("Received {} message(s) from NOTIFICATION_EXECUTOR queue: {}", messageCount, queueUrl);
            } else {
                if (count % 30 == 0) {
                    log.debug("No messages in NOTIFICATION_EXECUTOR queue (poll attempt #{})", count);
                }
            }

            List<Message> messages = response.messages();
            List<CompletableFuture<Boolean>> futures = new ArrayList<>(messages.size());
            for (Message message : messages) {
                String body = message.body();
                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return processExecutorMessage(body);
                    } catch (Throwable t) {
                        log.error("Error processing executor message, will retry: {}", body, t);
                        return false;
                    }
                }, executor);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();
            for (int i = 0; i < messages.size(); i++) {
                Message message = messages.get(i);
                if (Boolean.TRUE.equals(futures.get(i).getNow(false))) {
                    deleteMessage(queueUrl, message, sqsClient);
                    log.debug("Deleted processed message from NOTIFICATION_EXECUTOR queue");
                } else {
                    log.warn("Message not processed successfully, will remain in queue for retry: {}", message.body());
                }
            }
        } catch (Exception ex) {
            log.error("Failed to poll NOTIFICATION_EXECUTOR queue (poll attempt #{})", count, ex);
        } finally {
            executorPolling.set(false);
        }
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
            return false;
        }

        return Boolean.TRUE.equals(template.execute(status -> {
            try {
                log.info("Processing notification executor message: {}", rawMessage);

                // Check if receipt exists and its status
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
            } catch (IllegalArgumentException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("Notification receipt not found")) {
                    log.warn("Notification receipt not found in executeReceipt. This may be an old message. Deleting from queue. Error: {}", ex.getMessage());
                    return true;
                }
                log.error("Failed to process executor message: {}", rawMessage, ex);
                status.setRollbackOnly();
                // executeReceipt already marked receipt as FAILED via markReceiptFailedInNewTransaction.
                // Return true to delete SQS message — no point retrying.
                return true;
            } catch (Exception ex) {
                log.error("Failed to process executor message: {}", rawMessage, ex);
                status.setRollbackOnly();
                // executeReceipt already marked receipt as FAILED via markReceiptFailedInNewTransaction.
                // Return true to delete SQS message — no point retrying.
                return true;
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