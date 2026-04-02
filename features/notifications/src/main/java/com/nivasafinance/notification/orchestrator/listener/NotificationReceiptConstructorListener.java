package com.nivasafinance.notification.orchestrator.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.MessageProvider;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.notification.orchestrator.entity.NotificationConfig;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.repository.NotificationConfigRepository;
import com.nivasafinance.notification.orchestrator.service.DataProviderExecutor;
import com.nivasafinance.notification.orchestrator.service.NotificationReceiptService;
import com.nivasafinance.notification.orchestrator.service.NotificationRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PreDestroy;

/**
 * Listener that polls the NOTIFICATION queue and creates NotificationReceipts.
 * This is responsible for the receipt construction phase:
 * 1. Receives notification records from NOTIFICATION queue
 * 2. Executes data providers to fetch recipient information
 * 3. Creates NotificationReceipt entities for each recipient
 * 4. Publishes receipts to NOTIFICATION_EXECUTOR queue for execution
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationReceiptConstructorListener {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String TIMEZONE = "Asia/Kolkata";

    private final ObjectMapper objectMapper;
    private final NotificationRecordService notificationRecordService;
    private final NotificationConfigRepository notificationConfigRepository;
    private final DataProviderExecutor dataProviderExecutor;
    private final NotificationReceiptService notificationReceiptService;
    private final MessagePublisherFactory messagePublisherFactory;
    private final MessagingProperties messagingProperties;
    private final ObjectProvider<SqsClient> sqsClientProvider;
    private final PlatformTransactionManager transactionManager;

    private static final long ERROR_BACKOFF_MS = 5_000L;

    private volatile boolean running;
    private Thread pollerThread;
    private ExecutorService executor;

    @jakarta.annotation.PostConstruct
    public void start() {
        int poolSize = Math.max(1, messagingProperties.getSqs().getMaxMessages());
        java.util.concurrent.atomic.AtomicInteger threadNumber = new java.util.concurrent.atomic.AtomicInteger(0);
        executor = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r, "notification-receipt-constructor-" + threadNumber.incrementAndGet());
            t.setDaemon(false);
            return t;
        });

        if (messagingProperties.getProvider() != MessageProvider.SQS) {
            log.info("NotificationReceiptConstructorListener: provider is not SQS, skipping continuous polling");
            return;
        }

        SqsClient sqsClient = sqsClientProvider.getIfAvailable();
        if (sqsClient == null) {
            log.warn("NotificationReceiptConstructorListener: SqsClient not available, skipping continuous polling");
            return;
        }

        running = true;
        pollerThread = new Thread(this::pollLoop, "notification-receipt-constructor-poller");
        pollerThread.setDaemon(true);
        pollerThread.start();
        log.info("NotificationReceiptConstructorListener started. Pool size: {}", poolSize);
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
        String queueUrl = messagingProperties.getSqs().resolveQueueUrl(QueueType.NOTIFICATION);
        SqsClient sqsClient = sqsClientProvider.getIfAvailable();

        while (running && sqsClient != null) {
            try {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .waitTimeSeconds(messagingProperties.getSqs().getWaitTimeSeconds())
                        .maxNumberOfMessages(messagingProperties.getSqs().getMaxMessages())
                        .build();

                List<Message> messages = sqsClient.receiveMessage(request).messages();

                List<CompletableFuture<Boolean>> futures = new ArrayList<>(messages.size());
                for (Message message : messages) {
                    String body = message.body();
                    CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            return processMessage(body);
                        } catch (Throwable t) {
                            log.error("Error processing notification record: {}", body, t);
                            return false;
                        }
                    }, executor);
                    futures.add(future);
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();

                for (int i = 0; i < messages.size(); i++) {
                    if (Boolean.TRUE.equals(futures.get(i).getNow(false))) {
                        deleteMessage(queueUrl, messages.get(i), sqsClient);
                    }
                }
            } catch (Exception ex) {
                if (running) {
                    log.error("Error in NOTIFICATION queue poll loop", ex);
                    try { Thread.sleep(ERROR_BACKOFF_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        log.info("NotificationReceiptConstructorListener poll loop stopped");
    }

    /**
     * Process a notification record directly by recordId.
     * Useful for local testing when not using SQS.
     * 
     * @param recordId The UUID of the notification record to process
     * @return true if processed successfully, false otherwise
     */
    public boolean processRecordById(UUID recordId) {
        Map<String, Object> messageMap = Map.of("recordId", recordId.toString());
        String messageBody;
        try {
            messageBody = objectMapper.writeValueAsString(messageMap);
        } catch (Exception ex) {
            log.error("Failed to serialize message for recordId: {}", recordId, ex);
            return false;
        }
        return processMessage(messageBody);
    }

    private boolean processMessage(String rawMessage) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return Boolean.TRUE.equals(template.execute(status -> handleMessageWithinTransaction(rawMessage, status)));
    }

    private boolean handleMessageWithinTransaction(String rawMessage, TransactionStatus status) {
        log.info("Processing notification receipt message: {}", rawMessage);

        Map<String, Object> messageMap = parseMessage(rawMessage);

        // Only recordId is published - load the full record
        UUID recordId = UUID.fromString((String) messageMap.get("recordId"));

        try {
            // Load the notification record by recordId
            Optional<NotificationRecord> recordOpt = notificationRecordService.findById(recordId);
            if (recordOpt.isEmpty()) {
                log.warn("Notification record not found: {}. This may be an old message or the record was deleted. Deleting message from queue.", recordId);
                // Return true to delete the message from queue (don't retry forever)
                return true;
            }

            NotificationRecord record = recordOpt.get();

            if (record.getStatus() == NotificationStatus.COMPLETED) {
                log.info("Notification record {} is already COMPLETED, skipping processing. Deleting message from queue.", recordId);
                return true;
            }
            if (record.getStatus() == NotificationStatus.FAILED || record.getStatus() == NotificationStatus.SKIPPED) {
                log.info("Notification record {} is already in terminal state {}, deleting message from queue.", recordId, record.getStatus());
                return true;
            }

            if (!notificationRecordService.updateStatusIfExists(recordId, NotificationStatus.PROCESSING)) {
                log.warn("Failed to update status for record {} - record may have been deleted. Deleting message from queue.", recordId);
                return true;
            }

            // Get configId, eventType, and payload from the record
            Long configId = record.getNotificationConfigId();
            Map<String, Object> notificationPayload = record.getNotificationPayload();
            
            // Get eventType from details JSONB column
            Map<String, Object> details = record.getDetails();
            if (details == null || !details.containsKey("event_type")) {
                throw new IllegalStateException("Notification record details missing event_type");
            }
            String eventType = (String) details.get("event_type");

            if (notificationPayload == null || notificationPayload.isEmpty()) {
                throw new IllegalStateException("Notification payload is empty");
            }

            // Load the notification config
            NotificationConfig config = notificationConfigRepository.findById(configId)
                    .orElseThrow(() -> new IllegalStateException("NotificationConfig not found for id " + configId));

            Map<String, Object> configMap = config.getConfig();

            // Use the payload fields directly as query parameters
            // The parameter names in the DataProvider query must match the keys in this map
            Map<String, Object> queryParams = new HashMap<>(notificationPayload);

            // Get data provider configuration
            Map<String, Object> dataProviderConfig = asMap(configMap.get("dataProvider"), "config.dataProvider");
            if (dataProviderConfig == null) {
                throw new IllegalStateException("Notification config missing dataProvider definition");
            }
            String providerKey = (String) dataProviderConfig.get("key");
            if (providerKey == null) {
                throw new IllegalStateException("Notification config missing dataProvider key");
            }

            // Execute data provider with payload fields as parameters
            Map<String, String> dataProviderResult = dataProviderExecutor.executeDataProvider(
                    providerKey,
                    queryParams
            );

            // Get recipients from config - no defaults, each recipient must specify mode and channelType
            List<Map<String, Object>> recipients = asListOfMaps(configMap.get("recipients"), "config.recipients");
            if (recipients == null || recipients.isEmpty()) {
                throw new IllegalStateException("Notification config must specify at least one recipient");
            }

            // Create receipt for each recipient
            log.info("Creating receipts for {} recipient(s). Data provider result keys: {}", 
                    recipients.size(), dataProviderResult.keySet());
            
            int receiptsCreated = 0;
            for (Map<String, Object> recipientDefinition : recipients) {
                log.info("Processing recipient definition: {}", recipientDefinition);
                boolean receiptCreated = createReceipt(recordId, eventType, notificationPayload, dataProviderResult, recipientDefinition);
                if (receiptCreated) {
                    receiptsCreated++;
                }
            }

            // If no receipts were created (all recipients skipped due to missing template/contact),
            // mark the record as SKIPPED
            if (receiptsCreated == 0) {
                log.warn("No receipts were created for record {} - all recipients were skipped. Marking record as SKIPPED.", recordId);
                notificationRecordService.updateStatusIfExists(recordId, NotificationStatus.SKIPPED);
            } else {
                log.info("Created {} receipt(s) for record {}. Marking record as COMPLETED.", receiptsCreated, recordId);
                notificationRecordService.updateStatusIfExists(recordId, NotificationStatus.COMPLETED);
            }
            return true;
        } catch (Exception ex) {
            log.error("Failed to process notification record {}", recordId, ex);
            Map<String, Object> errorJson = buildErrorJson(ex);
            status.setRollbackOnly();
            try {
                TransactionTemplate requiresNew = new TransactionTemplate(transactionManager);
                requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                requiresNew.execute(s -> {
                    notificationRecordService.updateStatusAndErrorIfExists(recordId, NotificationStatus.FAILED, errorJson);
                    return null;
                });
                log.warn("Marked notification record {} as FAILED. Deleting message from queue.", recordId);
                return true;
            } catch (Exception updateEx) {
                log.error("Failed to mark notification record {} as FAILED; record stays INITIATED. Message will be retried by SQS.", recordId, updateEx);
                return false;
            }
        }
    }

    private Map<String, Object> parseMessage(String rawMessage) {
        try {
            return objectMapper.readValue(rawMessage, MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse SQS message body", ex);
        }
    }

    private Map<String, Object> buildErrorJson(Exception ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        map.put("exceptionType", ex.getClass().getSimpleName());
        return map;
    }

    /**
     * Creates a notification receipt for a recipient.
     * 
     * @param recordId The notification record ID
     * @param eventType The event type
     * @param notificationPayload The notification payload
     * @param dataProviderResult The data provider result
     * @param recipientDefinition The recipient definition from config
     * @return true if receipt was created, false if skipped (missing template/contact)
     */
    private boolean createReceipt(UUID recordId,
                               String eventType,
                               Map<String, Object> notificationPayload,
                               Map<String, String> dataProviderResult,
                               Map<String, Object> recipientDefinition) {

        String recipientType = (String) recipientDefinition.get("recipient");
        String recipientKey = (String) recipientDefinition.get("recipientKey");
        String recipientTemplateKey = (String) recipientDefinition.get("recipientTemplateKey");

        log.info("Creating receipt for recipient: type={}, key={}, templateKey={}", 
                recipientType, recipientKey, recipientTemplateKey);

        // Mode and channelType are required per recipient - no defaults
        String mode = (String) recipientDefinition.get("mode");
        if (mode == null || mode.isBlank()) {
            log.error("Skipping recipient {} because mode is not specified in recipient definition: {}", 
                    recipientKey, recipientDefinition);
            return false;
        }

        String channelType = (String) recipientDefinition.get("channelType");
        if (channelType == null || channelType.isBlank()) {
            log.error("Skipping recipient {} because channelType is not specified in recipient definition: {}", 
                    recipientKey, recipientDefinition);
            return false;
        }

        log.info("Checking data provider result for recipientKey '{}'. Available keys: {}", 
                recipientKey, dataProviderResult.keySet());
        String recipientContact = getCaseInsensitive(dataProviderResult, recipientKey);
        if (recipientContact == null || recipientContact.isBlank()) {
            String valueStatus = recipientContact == null ? "null" : "empty string";
            log.warn("Skipping recipient {} because contact value is {} for key '{}'. " +
                    "Available keys in data provider result: {}. " +
                    "This usually means the query returned null/empty for this field (e.g., no advisor assigned, no mobile number).", 
                    recipientKey, valueStatus, recipientKey, dataProviderResult.keySet());
            return false;
        }

        log.info("Checking data provider result for templateKey '{}'", recipientTemplateKey);
        String templateIdentifier = getCaseInsensitive(dataProviderResult, recipientTemplateKey);
        if (templateIdentifier == null || templateIdentifier.isBlank()) {
            log.warn("Skipping recipient {} because template identifier not found for key '{}'. " +
                    "Available keys in data provider result: {}", 
                    recipientKey, recipientTemplateKey, dataProviderResult.keySet());
            return false;
        }

        log.info("All validations passed. Creating receipt for recipient: contact={}, template={}, mode={}, channel={}", 
                recipientContact, templateIdentifier, mode, channelType);

        // Build message payload: combine notification payload with data provider result
        Map<String, Object> messagePayload = new HashMap<>(notificationPayload);
        messagePayload.putAll(dataProviderResult);

        // Per-recipient notification bucket from config (e.g. different bucket for STAFF vs ADVISOR app)
        String recipientNotificationBucket = getRecipientNotificationBucket(recipientDefinition);
        if (recipientNotificationBucket != null && !recipientNotificationBucket.isBlank()) {
            messagePayload.put("notification_bucket", recipientNotificationBucket);
        }

        // Extract entity ID from notification payload for details
        String entityId = notificationPayload.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().endsWith("id") &&
                        (entry.getKey().equals("leadId") || entry.getKey().equals("personId") ||
                         entry.getKey().equals("documentId") || entry.getKey().equals("id")))
                .map(entry -> entry.getValue() != null ? entry.getValue().toString() : null)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);

        Map<String, Object> details = new HashMap<>();
        details.put("recipient_type", recipientType);
        details.put("event_type", eventType);
        details.put("appUser", recipientContact); // so tracking can resolve app user from receipt when messagePayload key differs
        if (entityId != null) {
            details.put("entity_id", entityId);
        }

        // Build schedules from preferred times if available
        // Schedules contain preferred_call_start_time and preferred_call_end_time as a range
        // A job runs every 15 minutes to pick receipts within the time range
        // Uses recipient-specific preferred times (advisor vs lead) if available
        List<Map<String, Object>> schedules = buildSchedules(dataProviderResult, recipientDefinition, recipientType);

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

        // Only publish to executor queue immediately if no schedule exists
        // If schedule exists, the scheduled job will publish it when preferred time is reached
        if (schedules == null || schedules.isEmpty()) {
            // No schedule - publish immediately for immediate execution
            // Check if receipt is already COMPLETED to avoid duplicate sends
            if (receipt.getStatus() == NotificationStatus.COMPLETED) {
                log.info("Receipt {} is already COMPLETED, skipping publish to executor queue", receipt.getId());
                return true;
            }
            
            // Check if already published (from remarks)
            Map<String, Object> remarks = receipt.getRemarks();
            boolean alreadyPublished = remarks != null && 
                    "PUBLISHED".equals(remarks.get("executorQueueStatus"));
            
            if (alreadyPublished) {
                log.info("Receipt {} already published to executor queue, skipping duplicate publish", receipt.getId());
                return true;
            }
            
            try {
                log.info("Publishing receipt {} to NOTIFICATION_EXECUTOR queue (no schedule, immediate execution)", receipt.getId());
                messagePublisherFactory.getPublisher().publish(
                        QueueType.NOTIFICATION_EXECUTOR,
                        receipt.getId().toString(),
                        Map.of("receiptId", receipt.getId().toString())
                );
                
                // Update remarks to mark as published (prevent duplicate publishes)
                Map<String, Object> updatedRemarks = new HashMap<>(remarks != null ? remarks : Map.of());
                updatedRemarks.put("executorQueueStatus", "PUBLISHED");
                updatedRemarks.put("publishedAt", System.currentTimeMillis());
                receipt.setRemarks(updatedRemarks);
                notificationReceiptService.save(receipt);
                
                log.info("Created notification receipt {} for record {} and published to executor queue (immediate execution)", 
                        receipt.getId(), recordId);
            } catch (Exception ex) {
                log.error("Failed to publish receipt {} to executor queue", receipt.getId(), ex);
                throw ex;
            }
        } else {
            // Has schedule - don't publish yet, scheduled job will handle it
            log.info("Created notification receipt {} for record {} with schedule: {}. Will be published when preferred time is reached", 
                    receipt.getId(), recordId, schedules);
        }
        
        return true; // Receipt was successfully created
    }

    /**
     * Reads optional per-recipient notification bucket from recipient definition (for FIREBASE/ANDROID).
     * Uses {@code notificationBucket} only. Returns null if not set or blank.
     */
    private String getRecipientNotificationBucket(Map<String, Object> recipientDefinition) {
        if (recipientDefinition == null) {
            return null;
        }
        Object notificationBucket = recipientDefinition.get("notificationBucket");
        if (notificationBucket != null && !notificationBucket.toString().isBlank()) {
            return notificationBucket.toString();
        }
        return null;
    }

    /**
     * Builds schedules from preferred times if available.
     * Uses recipient-specific preferred times based on recipient type:
     * - For ADVISOR: Uses advisor preferred times from n_advisor.other_details
     * - For LEAD: Uses lead preferred times from n_lead.other_details
     * 
     * The data provider query should return:
     * - For advisor: preferred times from n_advisor.other_details->>'preferredCallStartTime'
     * - For lead: preferred times from n_lead.other_details->>'preferredCallStartTime'
     * 
     * The query should alias these as 'preferred_call_start_time' and 'preferred_call_end_time'
     * (or recipient-specific like 'advisor_preferred_call_start_time' if both are returned).
     * 
     * If not present, returns empty list (will be executed immediately by the executor).
     */
    private List<Map<String, Object>> buildSchedules(Map<String, String> dataProviderResult,
                                                      Map<String, Object> recipientDefinition,
                                                      String recipientType) {
        // Check if recipient definition has explicit schedules override
        if (recipientDefinition.containsKey("schedules")) {
            return asListOfMapsOrEmpty(recipientDefinition.get("schedules"), "recipient.schedules");
        }

        // Try to get preferred times from data provider result
        // First try recipient-specific fields, then fall back to generic fields
        String preferredStartTimeStr = null;
        String preferredEndTimeStr = null;

        if ("ADVISOR".equalsIgnoreCase(recipientType)) {
            // For advisor, try advisor-specific fields first, then generic
            preferredStartTimeStr = getCaseInsensitive(dataProviderResult, "advisor_preferred_call_start_time");
            preferredEndTimeStr = getCaseInsensitive(dataProviderResult, "advisor_preferred_call_end_time");
            
            if (preferredStartTimeStr == null || preferredStartTimeStr.isBlank()) {
                preferredStartTimeStr = getCaseInsensitive(dataProviderResult, "preferred_call_start_time");
                preferredEndTimeStr = getCaseInsensitive(dataProviderResult, "preferred_call_end_time");
            }
        } else if ("LEAD".equalsIgnoreCase(recipientType)) {
            // For lead, try lead-specific fields first, then generic
            preferredStartTimeStr = getCaseInsensitive(dataProviderResult, "lead_preferred_call_start_time");
            preferredEndTimeStr = getCaseInsensitive(dataProviderResult, "lead_preferred_call_end_time");
            
            if (preferredStartTimeStr == null || preferredStartTimeStr.isBlank()) {
                preferredStartTimeStr = getCaseInsensitive(dataProviderResult, "preferred_call_start_time");
                preferredEndTimeStr = getCaseInsensitive(dataProviderResult, "preferred_call_end_time");
            }
        } else {
            // For other recipient types, use generic fields
            preferredStartTimeStr = getCaseInsensitive(dataProviderResult, "preferred_call_start_time");
            preferredEndTimeStr = getCaseInsensitive(dataProviderResult, "preferred_call_end_time");
        }

        if (preferredStartTimeStr != null && !preferredStartTimeStr.isBlank() &&
            preferredEndTimeStr != null && !preferredEndTimeStr.isBlank()) {
            try {
                // Parse the time strings (format: "HH:mm:ss" or "HH:mm")
                LocalTime preferredStartTime = parseTime(preferredStartTimeStr);
                LocalTime preferredEndTime = parseTime(preferredEndTimeStr);

                Map<String, Object> schedule = new HashMap<>();
                schedule.put("preferred_call_start_time", preferredStartTime.format(TIME_FORMATTER));
                schedule.put("preferred_call_end_time", preferredEndTime.format(TIME_FORMATTER));
                schedule.put("timezone", TIMEZONE);

                log.info("Built schedule for recipient type {}: {} - {}", 
                        recipientType, preferredStartTime, preferredEndTime);
                return List.of(schedule);
            } catch (Exception ex) {
                log.warn("Failed to parse preferred times for recipient type {}: start={}, end={}", 
                        recipientType, preferredStartTimeStr, preferredEndTimeStr, ex);
            }
        } else {
            log.debug("No preferred times found for recipient type {} in data provider result. Available keys: {}", 
                    recipientType, dataProviderResult.keySet());
        }

        // No preferred times - return empty list (will be executed immediately)
        return List.of();
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            throw new IllegalArgumentException("Time string cannot be null or blank");
        }

        // Try HH:mm:ss format first
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (Exception e) {
            // Try HH:mm format
            try {
                return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e2) {
                throw new IllegalArgumentException("Invalid time format: " + timeStr, e2);
            }
        }
    }

    private void deleteMessage(String queueUrl, Message message, SqsClient sqsClient) {
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

    /**
     * Performs case-insensitive lookup in a map.
     * First tries exact match, then case-insensitive match.
     * 
     * @param map The map to search in
     * @param key The key to look for (case-insensitive)
     * @return The value if found, null otherwise
     */
    private String getCaseInsensitive(Map<String, String> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        
        // First try exact match (fast path)
        String value = map.get(key);
        if (value != null) {
            return value;
        }
        
        // Then try case-insensitive match
        return map.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}

