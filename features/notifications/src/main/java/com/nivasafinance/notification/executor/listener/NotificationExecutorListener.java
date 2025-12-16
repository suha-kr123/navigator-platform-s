package com.nivasafinance.notification.executor.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    /**
     * Polls the NOTIFICATION_EXECUTOR queue and processes receipts.
     * This listener picks up receipts that are ready to be sent and executes them.
     */
    @Scheduled(fixedDelayString = "${messaging.sqs.poll-delay-ms:1000}")
    public void pollExecutorQueue() {
        // Only poll SQS if provider is SQS and SqsClient is available
        if (messagingProperties.getProvider() != MessageProvider.SQS) {
            return;
        }

        SqsClient sqsClient = sqsClientProvider.getIfAvailable();
        if (sqsClient == null) {
            return;
        }

        if (!executorPolling.compareAndSet(false, true)) {
            return;
        }

        try {
            String queueUrl = messagingProperties.getSqs().resolveQueueUrl(QueueType.NOTIFICATION_EXECUTOR);
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .waitTimeSeconds(messagingProperties.getSqs().getWaitTimeSeconds())
                    .maxNumberOfMessages(messagingProperties.getSqs().getMaxMessages())
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(request);
            for (Message message : response.messages()) {
                boolean processed = processExecutorMessage(message.body());
                if (processed) {
                    deleteMessage(queueUrl, message, sqsClient);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to poll NOTIFICATION_EXECUTOR queue", ex);
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
                log.info("Processing executor message: {}", rawMessage);
                Map<String, Object> messageMap = parseMessage(rawMessage);
                String receiptIdStr = (String) messageMap.get("receiptId");
                if (receiptIdStr == null) {
                    log.error("Executor message missing receiptId: {}", rawMessage);
                    return false;
                }

                UUID receiptId = UUID.fromString(receiptIdStr);
                log.info("Executing receipt {}", receiptId);
                notificationReceiptService.executeReceipt(receiptId);
                log.info("Successfully executed receipt {}", receiptId);
                return true;
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

