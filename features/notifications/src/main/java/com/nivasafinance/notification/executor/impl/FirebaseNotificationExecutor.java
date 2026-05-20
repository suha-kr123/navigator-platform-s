package com.nivasafinance.notification.executor.impl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nivasafinance.notification.executor.NotificationExecutor;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.entity.NotificationTemplate;
import com.nivasafinance.notification.orchestrator.repository.NotificationTemplateRepository;
import com.nivasafinance.notification.orchestrator.service.DeviceService;
import com.nivasafinance.notification.orchestrator.service.AppNotificationTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** FCM executor: resolves app user, loads tokens from n_device, sends and tracks. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class FirebaseNotificationExecutor implements NotificationExecutor {

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceService deviceService;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final AppNotificationTrackingService trackingService;
    private final ObjectMapper objectMapper;

    @Override
    public void send(NotificationReceipt receipt, String renderedMessage) throws Exception {

        String recipientContact = receipt.getRecipientContact();
        String templateIdentifier = receipt.getTemplateIdentifier();
        Map<String, Object> messagePayload = receipt.getMessagePayload();

        log.info("Sending Firebase notification. Recipient contact: {}, Template: {}",
                recipientContact, templateIdentifier);

        String appUser = extractAppUser(recipientContact, messagePayload);
        if (appUser == null || appUser.isBlank()) {
            throw new IllegalStateException("App user not found: recipientContact or messagePayload.appUser must be set. " +
                    "Cannot determine which app user to send notification to.");
        }

        log.info("Sending Firebase notification to app user: {}, Template: {}", appUser, templateIdentifier);

        List<String> notificationTokens = deviceService.getActiveNotificationTokensForUser(appUser);
        
        if (notificationTokens == null || notificationTokens.isEmpty()) {
            log.warn("No active notification tokens found for app user: {}. Skipping notification.", appUser);
            throw new IllegalStateException("No active notification tokens found for app user: " + appUser);
        }

        log.info("Found {} active device(s) for app user: {}", notificationTokens.size(), appUser);

        NotificationTemplate template = notificationTemplateRepository.findByIdentifier(templateIdentifier)
                .orElseThrow(() -> new IllegalStateException("NotificationTemplate not found: " + templateIdentifier));

        String title = extractTitle(template, messagePayload);
        String body = extractBody(template, messagePayload, renderedMessage);

        Map<String, String> dataPayload = buildDataPayload(messagePayload);
        dataPayload.put("receiptId", receipt.getId().toString());

        // Android channel id (optional)
        String notificationBucket = getNotificationBucket(template, messagePayload);
        log.debug("Notification bucket for template {}: {}", templateIdentifier, notificationBucket != null ? notificationBucket : "(not set)");

        // Phase 1 — FCM I/O only, no DB writes.
        // Collecting results here before touching any table avoids the deadlock caused by
        // interleaving n_device writes (deactivation) with n_notification_receipt writes (tracking).
        List<String> successTokens = new ArrayList<>();
        Map<String, String> successMessageIds = new HashMap<>();
        List<String> invalidTokens = new ArrayList<>();
        Map<String, String[]> failedTokenErrors = new LinkedHashMap<>(); // token → [errorCode, errorMessage]

        for (String notificationToken : notificationTokens) {
            try {
                String providerMessageId = sendToToken(notificationToken, title, body, dataPayload, notificationBucket);
                successTokens.add(notificationToken);
                successMessageIds.put(notificationToken, providerMessageId);
                log.debug("FCM send succeeded for token: {}... (app user: {}), messageId: {}",
                        notificationToken.substring(0, Math.min(20, notificationToken.length())), appUser, providerMessageId);
            } catch (FirebaseMessagingException e) {
                String errorCode = e.getErrorCode() != null ? e.getErrorCode().name() : "UNKNOWN";
                log.error("FCM send failed for app user {}: {}", appUser, errorCode, e);
                failedTokenErrors.put(notificationToken, new String[]{errorCode, e.getMessage()});
                MessagingErrorCode messagingCode = e.getMessagingErrorCode();
                if (messagingCode == MessagingErrorCode.INVALID_ARGUMENT || messagingCode == MessagingErrorCode.UNREGISTERED) {
                    invalidTokens.add(notificationToken);
                    log.warn("Token flagged as invalid for app user {}: {}", appUser, errorCode);
                }
            } catch (Exception e) {
                log.error("Unexpected FCM error for app user {}", appUser, e);
                failedTokenErrors.put(notificationToken, new String[]{"UNEXPECTED_ERROR",
                        e.getMessage() != null ? e.getMessage() : "Unknown error"});
            }
        }

        // Phase 2 — n_device WRITE (deactivate invalid tokens).
        // Must happen before Phase 3 so that within this transaction the lock order is
        // always n_device → n_notification_receipt, preventing circular waits.
        if (!invalidTokens.isEmpty()) {
            log.warn("Deactivating {} invalid token(s) for app user {}", invalidTokens.size(), appUser);
            try {
                int deactivatedCount = deviceService.deactivateDevicesByTokens(appUser, invalidTokens);
                log.info("Deactivated {} invalid device(s) for app user {}", deactivatedCount, appUser);
            } catch (Exception e) {
                log.error("Failed to deactivate invalid tokens for app user {}", appUser, e);
            }
        }

        // Phase 3 — n_notification_receipt WRITE (tracking records).
        // n_device is always written in Phase 2 before we write n_notification_receipt here,
        // keeping lock ordering consistent across all transactions.
        for (String token : successTokens) {
            trackingService.saveNotificationTracking(receipt, token, successMessageIds.get(token), templateIdentifier);
        }
        for (Map.Entry<String, String[]> entry : failedTokenErrors.entrySet()) {
            trackingService.saveFailedNotificationTracking(receipt, entry.getKey(),
                    entry.getValue()[0], entry.getValue()[1], templateIdentifier);
        }

        int successCount = successTokens.size();
        int failureCount = failedTokenErrors.size();

        if (successCount == 0) {
            throw new IllegalStateException(
                    String.format("Failed to send Firebase notification to app user %s. " +
                            "All %d device(s) failed. Last error: %s",
                            appUser, notificationTokens.size(),
                            failureCount > 0 ? "See logs" : "No tokens"));
        }

        if (failureCount > 0) {
            log.warn("Partially successful: {} succeeded, {} failed for app user {}",
                    successCount, failureCount, appUser);
        }

        log.info("Successfully sent Firebase notification to app user {}. " +
                "Sent to {}/{} device(s)", appUser, successCount, notificationTokens.size());
    }


    private String extractAppUser(String recipientContact, Map<String, Object> messagePayload) {
        if (recipientContact != null && !recipientContact.isBlank() && recipientContact.length() < 100) {
            return recipientContact;
        }
        if (messagePayload != null) {
            Object v = messagePayload.get("appUser");
            if (v != null && !v.toString().isBlank()) {
                return v.toString();
            }
        }
        return null;
    }

    private String extractTitle(NotificationTemplate template, Map<String, Object> messagePayload) {
        if (messagePayload != null && messagePayload.containsKey("title")) {
            Object titleObj = messagePayload.get("title");
            String title = titleObj != null ? titleObj.toString() : "";
            return replacePlaceholders(title, messagePayload);
        }
        if (template != null && template.getIdentifier() != null) {
            return template.getIdentifier();
        }
        return "Notification";
    }

    private String extractBody(NotificationTemplate template, Map<String, Object> messagePayload, String renderedMessage) {
        if (renderedMessage != null && !renderedMessage.isBlank()) {
            return replacePlaceholders(renderedMessage, messagePayload);
        }
        if (messagePayload != null && messagePayload.containsKey("body")) {
            return replacePlaceholders(messagePayload.get("body").toString(), messagePayload);
        }
        if (messagePayload != null && messagePayload.containsKey("message")) {
            return replacePlaceholders(messagePayload.get("message").toString(), messagePayload);
        }
        if (template != null && template.getDetail() != null) {
            return replacePlaceholders(template.getDetail(), messagePayload);
        }

        return "You have a new notification";
    }

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    private String replacePlaceholders(String text, Map<String, Object> messagePayload) {
        if (text == null || messagePayload == null) {
            return text;
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String placeholderName = matcher.group(1);
            Object valueObj = getValueIgnoreCase(messagePayload, placeholderName);
            String value = valueObj != null ? valueObj.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private Object getValueIgnoreCase(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (key.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Map<String, String> buildDataPayload(Map<String, Object> messagePayload) {
        Map<String, String> dataPayload = new HashMap<>();
        if (messagePayload == null) {
            return dataPayload;
        }

        String metaDataJson = buildMetaDataIfConventionMatch(messagePayload);
        if (metaDataJson != null) {
            dataPayload.put("metaData", metaDataJson);
            return dataPayload;
        }

        for (Map.Entry<String, Object> entry : messagePayload.entrySet()) {
            if (shouldSkipDataPayloadKey(entry.getKey())) {
                continue;
            }
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            dataPayload.put(entry.getKey(), value);
        }
        return dataPayload;
    }

    private static boolean shouldSkipDataPayloadKey(String key) {
        if (key == null || key.startsWith("_")) {
            return true;
        }
        switch (key) {
            case "templateIdentifier":
            case "recipientContact":
            case "appUser":
            case "title":
            case "body":
            case "message":
                return true;
            default:
                return false;
        }
    }

    private String buildMetaDataIfConventionMatch(Map<String, Object> messagePayload) {
        Object metaDataObj = messagePayload.get("metaData");
        if (metaDataObj != null && metaDataObj instanceof String) {
            return (String) metaDataObj;
        }

        Object entityType = messagePayload.get("entityType");
        Object entityId = messagePayload.get("entityId");
        if (entityType != null && entityId != null) {
            Map<String, Object> metaData = new LinkedHashMap<>();
            metaData.put("entityType", entityType.toString());
            metaData.put("entityId", entityId.toString());
            Object parentType = messagePayload.get("parentEntityType");
            Object parentId = messagePayload.get("parentEntityId");
            if (parentType != null && parentId != null) {
                Map<String, Object> parentEntity = new LinkedHashMap<>();
                parentEntity.put("entityType", parentType.toString());
                parentEntity.put("entityId", parentId.toString());
                metaData.put("parentEntity", parentEntity);
            }
            return writeMetaDataJson(metaData);
        }

        Object taskId = messagePayload.get("taskIdentifier");
        Object leadId = messagePayload.get("leadIdentifier");
        if (taskId != null && leadId != null) {
            Map<String, Object> metaData = new LinkedHashMap<>();
            metaData.put("entityType", "TASK");
            metaData.put("entityId", taskId.toString());
            Map<String, Object> parentEntity = new LinkedHashMap<>();
            parentEntity.put("entityType", "LEAD");
            parentEntity.put("entityId", leadId.toString());
            metaData.put("parentEntity", parentEntity);
            return writeMetaDataJson(metaData);
        }

        return null;
    }

    private String writeMetaDataJson(Map<String, Object> metaData) {
        try {
            return objectMapper.writeValueAsString(metaData);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize metaData for FCM", e);
            return "{}";
        }
    }


    private String getNotificationBucket(NotificationTemplate template, Map<String, Object> messagePayload) {
        String key = "notification_bucket";
        if (messagePayload != null && messagePayload.containsKey(key)) {
            Object v = messagePayload.get(key);
            if (v != null && !v.toString().isBlank()) {
                return v.toString();
            }
        }
        if (template != null && template.getVariables() != null && template.getVariables().containsKey(key)) {
            Object v = template.getVariables().get(key);
            if (v != null && !v.toString().isBlank()) {
                return v.toString();
            }
        }
        return null;
    }

    private String sendToToken(String notificationToken, String title, String body, Map<String, String> dataPayload,
                              String notificationBucket) throws IOException, FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        com.google.firebase.messaging.AndroidNotification.Builder androidNotifBuilder =
                com.google.firebase.messaging.AndroidNotification.builder();
        if (notificationBucket != null && !notificationBucket.isBlank()) {
            androidNotifBuilder.setChannelId(notificationBucket);
        }

        Message.Builder messageBuilder = Message.builder()
                .setToken(notificationToken)
                .setNotification(notification)
                .putAllData(dataPayload)
                .setAndroidConfig(com.google.firebase.messaging.AndroidConfig.builder()
                        .setPriority(com.google.firebase.messaging.AndroidConfig.Priority.HIGH)
                        .setNotification(androidNotifBuilder.build())
                        .build());

        Message message = messageBuilder.build();

        String providerMessageId = firebaseMessaging.send(message);
        log.debug("Firebase notification sent successfully. Message ID: {}", providerMessageId);
        return providerMessageId;
    }

    @Override
    public String getMode() {
        return "FIREBASE";
    }

    @Override
    public String getChannelType() {
        return "ANDROID";
    }
}

