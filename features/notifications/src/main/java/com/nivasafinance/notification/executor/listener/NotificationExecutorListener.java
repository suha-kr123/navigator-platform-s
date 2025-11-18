package com.nivasafinance.notification.executor.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.notification.executor.entity.NotificationReceipt;
import com.nivasafinance.notification.executor.service.NotificationReceiptService;
import com.nivasafinance.notification.orchestrator.entity.NotificationConfig;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
import com.nivasafinance.notification.orchestrator.repository.NotificationConfigRepository;
import com.nivasafinance.notification.orchestrator.service.DataProviderExecutor;
import com.nivasafinance.notification.orchestrator.service.NotificationRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(name = "messaging.provider", havingValue = "SQS")
@RequiredArgsConstructor
@Slf4j
public class NotificationExecutorListener {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final NotificationRecordService notificationRecordService;
    private final NotificationConfigRepository notificationConfigRepository;
    private final DataProviderExecutor dataProviderExecutor;
    private final NotificationReceiptService notificationReceiptService;
    private final MessagePublisherFactory messagePublisherFactory;
    private final MessagingProperties messagingProperties;
    private final SqsClient sqsClient;
    private final PlatformTransactionManager transactionManager;

    private final AtomicBoolean polling = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${messaging.sqs.poll-delay-ms:1000}")
    public void pollQueue() {
        if (!polling.compareAndSet(false, true)) {
            return;
        }

        String queueUrl = messagingProperties.getSqs().resolveQueueUrl(QueueType.NOTIFICATION);
        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .waitTimeSeconds(messagingProperties.getSqs().getWaitTimeSeconds())
                    .maxNumberOfMessages(messagingProperties.getSqs().getMaxMessages())
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(request);
            for (Message message : response.messages()) {
                boolean processed = processMessage(message.body());
                if (processed) {
                    deleteMessage(queueUrl, message);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to poll SQS queue {}", queueUrl, ex);
        } finally {
            polling.set(false);
        }
    }

    private boolean processMessage(String rawMessage) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return Boolean.TRUE.equals(template.execute(status -> handleMessageWithinTransaction(rawMessage, status)));
    }

    private boolean handleMessageWithinTransaction(String rawMessage, TransactionStatus status) {
        log.info("Processing notification orchestration message: {}", rawMessage);

        Map<String, Object> messageMap = parseMessage(rawMessage);

        UUID recordId = UUID.fromString((String) messageMap.get("recordId"));
        Long configId = toLong(messageMap.get("configId"));
        String eventType = (String) messageMap.get("eventType");
        Map<String, Object> notificationPayload = asMap(messageMap.get("payload"), "payload");

        try {
            NotificationRecord record = notificationRecordService.findById(recordId)
                    .orElseThrow(() -> new IllegalStateException("NotificationRecord not found for id " + recordId));

            notificationRecordService.updateStatus(recordId, NotificationStatus.PROCESSING);

            NotificationConfig config = notificationConfigRepository.findById(configId)
                    .orElseThrow(() -> new IllegalStateException("NotificationConfig not found for id " + configId));

            Map<String, Object> configMap = config.getConfig();

            Map<String, Object> payloadWrapper = asMap(notificationPayload.get("payload"), "notification payload.wrapper");
            String leadId = payloadWrapper != null ? (String) payloadWrapper.get("leadId") : null;
            if (leadId == null || leadId.isBlank()) {
                throw new IllegalStateException("Lead ID missing from notification payload");
            }

            Map<String, Object> dataProviderConfig = asMap(configMap.get("dataProvider"), "config.dataProvider");
            if (dataProviderConfig == null) {
                throw new IllegalStateException("Notification config missing dataProvider definition");
            }
            String providerKey = (String) dataProviderConfig.get("key");
            if (providerKey == null) {
                throw new IllegalStateException("Notification config missing dataProvider key");
            }

            Map<String, String> dataProviderResult = dataProviderExecutor.executeDataProvider(
                    providerKey,
                    Map.of("leadId", leadId)
            );

            List<Map<String, Object>> recipients = asListOfMaps(configMap.get("recipients"), "config.recipients");
            if (recipients == null || recipients.isEmpty()) {
                throw new IllegalStateException("Notification config must specify at least one recipient");
            }

            for (Map<String, Object> recipientDefinition : recipients) {
                createReceipt(recordId, eventType, leadId, dataProviderResult, configMap, recipientDefinition);
            }

            notificationRecordService.updateStatus(recordId, NotificationStatus.COMPLETED);
            return true;
        } catch (Exception ex) {
            log.error("Failed to process notification record {}", recordId, ex);
            notificationRecordService.updateStatus(recordId, NotificationStatus.FAILED);
            status.setRollbackOnly();
            return false;
        }
    }

    private Map<String, Object> parseMessage(String rawMessage) {
        try {
            return objectMapper.readValue(rawMessage, MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse SQS message body", ex);
        }
    }

    private void createReceipt(UUID recordId,
                               String eventType,
                               String leadId,
                               Map<String, String> dataProviderResult,
                               Map<String, Object> configMap,
                               Map<String, Object> recipientDefinition) {

        String recipientType = (String) recipientDefinition.get("recipient");
        String recipientKey = (String) recipientDefinition.get("recipientKey");
        String recipientTemplateKey = (String) recipientDefinition.get("recipientTemplateKey");

        String recipientContact = dataProviderResult.get(recipientKey);
        if (recipientContact == null || recipientContact.isBlank()) {
            log.warn("Skipping recipient {} because contact not found in data provider result", recipientKey);
            return;
        }

        String templateIdentifier = dataProviderResult.get(recipientTemplateKey);
        if (templateIdentifier == null || templateIdentifier.isBlank()) {
            log.warn("Skipping recipient {} because template identifier not found for key {}", recipientKey, recipientTemplateKey);
            return;
        }

        String mode = (String) recipientDefinition.getOrDefault("mode", configMap.get("mode"));
        String channelType = (String) recipientDefinition.getOrDefault("channelType", configMap.get("channelType"));
        List<Map<String, Object>> schedules = asListOfMapsOrEmpty(
                recipientDefinition.containsKey("schedules")
                        ? recipientDefinition.get("schedules")
                        : configMap.get("schedules"),
                "recipient.schedules"
        );

        Map<String, Object> messagePayload = new HashMap<>(dataProviderResult);
        messagePayload.put("leadId", leadId);
        Map<String, Object> details = Map.of(
                "recipient_type", recipientType,
                "entity_id", leadId,
                "event_type", eventType
        );

        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID())
                .notificationRecordId(recordId)
                .mode(mode)
                .recipientContact(recipientContact)
                .channelType(channelType)
                .templateIdentifier(templateIdentifier)
                .messagePayload(messagePayload)
                .details(details)
                .schedules(schedules)
                .remarks(Map.of("status", NotificationStatus.INITIATED.name()))
                .build();

        receipt.setCreatedBy("system");
        receipt.setUpdatedBy("system");

        notificationReceiptService.save(receipt);

        messagePublisherFactory.getPublisher().publish(
                QueueType.NOTIFICATION_EXECUTOR,
                receipt.getId().toString(),
                Map.of("receiptId", receipt.getId().toString())
        );

        log.info("Created notification receipt {} for record {}", receipt.getId(), recordId);
    }

    private void deleteMessage(String queueUrl, Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
    }

    private Map<String, Object> asMap(Object value, String context) {
        if (value == null) {
            throw new IllegalStateException(context + " is required but was null");
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> typed = new HashMap<>();
            map.forEach((key, val) -> typed.put(String.valueOf(key), val));
            return typed;
        }
        throw new IllegalStateException(context + " must be a map but was " + value.getClass());
    }

    private List<Map<String, Object>> asListOfMaps(Object value, String context) {
        if (value == null) {
            throw new IllegalStateException(context + " is required but was null");
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalStateException(context + " must be a list but was " + value.getClass());
        }
        List<Map<String, Object>> typedList = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            typedList.add(asMap(list.get(i), context + "[" + i + "]"));
        }
        return typedList;
    }

    private List<Map<String, Object>> asListOfMapsOrEmpty(Object value, String context) {
        if (value == null) {
            return List.of();
        }
        return asListOfMaps(value, context);
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str) {
            return Long.parseLong(str);
        }
        throw new IllegalArgumentException("Cannot convert value to Long: " + value);
    }
}


