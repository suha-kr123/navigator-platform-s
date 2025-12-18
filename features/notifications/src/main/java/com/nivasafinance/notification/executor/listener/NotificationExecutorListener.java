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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import jakarta.annotation.PostConstruct;

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

    private final AtomicBoolean executorPolling = new AtomicBoolean(false);
    private int pollCount = 0;

    @PostConstruct
    public void init() {
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

    /**
     * Polls the NOTIFICATION_EXECUTOR queue and processes receipts.
     * This listener picks up receipts that are ready to be sent and executes them.
     */
    @Scheduled(fixedDelayString = "${messaging.sqs.poll-delay-ms:1000}")
    public void pollExecutorQueue() {
        pollCount++;
        
        // Log every 30 polls to confirm scheduled method is running
        if (pollCount % 30 == 0) {
            log.info("NotificationExecutorListener polling (attempt #{}). Provider: {}, SqsClient available: {}", 
                    pollCount, 
                    messagingProperties.getProvider(),
                    sqsClientProvider.getIfAvailable() != null);
        }
        
        // Only poll SQS if provider is SQS and SqsClient is available
        if (messagingProperties.getProvider() != MessageProvider.SQS) {
            if (pollCount % 30 == 0) {
                log.warn("Skipping poll - provider is not SQS: {}", messagingProperties.getProvider());
            }
            return;
        }

        SqsClient sqsClient = sqsClientProvider.getIfAvailable();
        if (sqsClient == null) {
            if (pollCount % 30 == 0) {
                log.warn("Skipping poll - SqsClient is not available");
            }
            return;
        }

        if (!executorPolling.compareAndSet(false, true)) {
            if (pollCount % 30 == 0) {
                log.warn("Skipping poll - previous poll still in progress");
            }
            return;
        }

        try {
            String queueUrl = messagingProperties.getSqs().resolveQueueUrl(QueueType.NOTIFICATION_EXECUTOR);
            int waitTime = messagingProperties.getSqs().getWaitTimeSeconds();
            int maxMessages = messagingProperties.getSqs().getMaxMessages();
            
            if (pollCount % 30 == 0) {
                log.info("Polling NOTIFICATION_EXECUTOR queue: {} (waitTime: {}s, maxMessages: {})", 
                        queueUrl, waitTime, maxMessages);
            }
            
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .waitTimeSeconds(waitTime)
                    .maxNumberOfMessages(maxMessages)
                    .build();

            log.debug("Calling receiveMessage on NOTIFICATION_EXECUTOR queue (poll attempt #{}, waitTime: {}s)", 
                    pollCount, waitTime);
            ReceiveMessageResponse response;
            try {
                response = sqsClient.receiveMessage(request);
            } catch (Exception receiveEx) {
                log.error("Exception during receiveMessage call on NOTIFICATION_EXECUTOR queue (poll attempt #{})", 
                        pollCount, receiveEx);
                throw receiveEx;
            }
            
            int messageCount = response.messages().size();
            
            if (messageCount > 0) {
                log.info("Received {} message(s) from NOTIFICATION_EXECUTOR queue: {}", messageCount, queueUrl);
            } else {
                if (pollCount % 30 == 0) {
                    log.debug("No messages in NOTIFICATION_EXECUTOR queue (poll attempt #{})", pollCount);
                }
            }
            
            for (Message message : response.messages()) {
                log.info("Processing message from NOTIFICATION_EXECUTOR queue. Message body: {}", message.body());
                boolean processed = processExecutorMessage(message.body());
                if (processed) {
                    deleteMessage(queueUrl, message, sqsClient);
                    log.debug("Deleted processed message from NOTIFICATION_EXECUTOR queue");
                } else {
                    log.warn("Message not processed successfully, will remain in queue for retry: {}", message.body());
                }
            }
        } catch (Exception ex) {
            log.error("Failed to poll NOTIFICATION_EXECUTOR queue (poll attempt #{})", pollCount, ex);
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

        return Boolean.TRUE.equals(template.execute(status -> {
            try {
                log.info("Processing notification executor message: {}", rawMessage);
                Map<String, Object> messageMap = parseMessage(rawMessage);
                
                // Support both "receiptId" and "messageId" for backward compatibility
                String receiptIdStr = (String) messageMap.get("receiptId");
                if (receiptIdStr == null) {
                    receiptIdStr = (String) messageMap.get("messageId");
                }
                
                if (receiptIdStr == null) {
                    log.error("Executor message missing receiptId/messageId. Available keys: {}. Message: {}", 
                            messageMap.keySet(), rawMessage);
                    return false;
                }

                UUID receiptId;
                try {
                    receiptId = UUID.fromString(receiptIdStr);
                } catch (IllegalArgumentException ex) {
                    log.error("Invalid receiptId format in executor message: {}. Message: {}", receiptIdStr, rawMessage, ex);
                    return false;
                }
                
                // Check if receipt exists and its status
                var receiptOpt = notificationReceiptService.findById(receiptId);
                if (receiptOpt.isEmpty()) {
                    log.warn("Notification receipt not found: {}. This may be an old message or the receipt was deleted. Deleting message from queue.", receiptId);
                    // Return true to delete the message from queue (don't retry forever)
                    return true;
                }
                
                var receipt = receiptOpt.get();
                // Check if receipt is already completed
                if (receipt.getStatus() == NotificationStatus.COMPLETED) {
                    log.info("Receipt {} is already COMPLETED, skipping execution. Deleting message from queue.", receiptId);
                    // Return true to delete the message from queue (already processed)
                    return true;
                }
                
                // Check if receipt is already failed (don't retry failed receipts automatically)
                if (receipt.getStatus() == NotificationStatus.FAILED) {
                    log.warn("Receipt {} is already FAILED, skipping execution. Deleting message from queue to prevent infinite retries.", receiptId);
                    // Return true to delete the message from queue (don't retry failed receipts)
                    return true;
                }
                
                log.info("Executing receipt {} (current status: {})", receiptId, receipt.getStatus());
                notificationReceiptService.executeReceipt(receiptId);
                log.info("Successfully executed receipt {}", receiptId);
                return true;
            } catch (IllegalArgumentException ex) {
                // Handle case where receipt doesn't exist (thrown by executeReceipt)
                if (ex.getMessage() != null && ex.getMessage().contains("Notification receipt not found")) {
                    log.warn("Notification receipt not found in executeReceipt. This may be an old message. Deleting from queue. Error: {}", ex.getMessage());
                    // Return true to delete the message from queue (don't retry forever)
                    return true;
                }
                log.error("Failed to process executor message: {}", rawMessage, ex);
                status.setRollbackOnly();
                return false;
            } catch (Exception ex) {
                log.error("Failed to process executor message: {}", rawMessage, ex);
                status.setRollbackOnly();
                return false;
            }
        }));
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

