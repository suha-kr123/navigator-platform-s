package com.nivasafinance.features.call.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.dto.CallNotificationResponse;
import com.nivasafinance.common.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CallNotificationRedisRepository {

    private static final String NOTIFICATION_KEY_PREFIX = "call:notification:";
    private static final String USER_NOTIFICATIONS_KEY_PREFIX = "call:user:";
    private static final String DUPLICATE_CHECK_KEY_PREFIX = "call:duplicate:";
    private static final Duration NOTIFICATION_TTL = Duration.ofHours(24);
    private static final Duration DUPLICATE_CHECK_TTL = Duration.ofHours(1);
    private static final int MIN_MAX_NOTIFICATIONS = 1;
    private static final int MAX_MAX_NOTIFICATIONS = 100;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${call.notification.max-per-user:5}")
    private int maxNotificationsPerUser;

    @PostConstruct
    public void validateConfig() {
        if (maxNotificationsPerUser < MIN_MAX_NOTIFICATIONS || maxNotificationsPerUser > MAX_MAX_NOTIFICATIONS) {
            throw new IllegalStateException(
                    String.format("call.notification.max-per-user must be between %d and %d, got: %d",
                            MIN_MAX_NOTIFICATIONS, MAX_MAX_NOTIFICATIONS, maxNotificationsPerUser));
        }
        log.info("Call notification max-per-user configured to: {}", maxNotificationsPerUser);
    }

    public void save(CallNotificationResponse notification, String dialWhomNumber) {
        if (notification == null) {
            log.error("Cannot save notification: notification is null");
            return;
        }
        
        if (notification.getCallSid() == null || notification.getCallSid().isBlank()) {
            log.error("Cannot save notification: CallSid is null or blank");
            return;
        }
        
        String eventType = notification.getEventType() != null && !notification.getEventType().isBlank()
                ? notification.getEventType()
                : "UNKNOWN";
        
        try {
            String notificationKey = NOTIFICATION_KEY_PREFIX + notification.getCallSid() + ":" + eventType;
            String notificationJson = objectMapper.writeValueAsString(notification);
            
            redisTemplate.opsForValue().set(notificationKey, notificationJson, NOTIFICATION_TTL);
            
            if (notification.getAgentEmail() != null && !notification.getAgentEmail().isBlank()) {
                String userKey = USER_NOTIFICATIONS_KEY_PREFIX + "email:" + notification.getAgentEmail();
                addNotificationToUser(userKey, notificationKey);
            }
            
            // Use DialWhomNumber (the agent number) for notifications
            // Fallback to callTo if DialWhomNumber is not provided
            String phone = (dialWhomNumber != null && !dialWhomNumber.isBlank()) 
                    ? dialWhomNumber 
                    : notification.getCallTo();
            if (phone != null && !phone.isBlank()) {
                // Normalize phone number before saving to Redis
                String normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(phone);
                if (normalizedPhone != null && !normalizedPhone.isBlank()) {
                    String userPhoneKey = USER_NOTIFICATIONS_KEY_PREFIX + "phone:" + normalizedPhone;
                    addNotificationToUser(userPhoneKey, notificationKey);
                }
            }
        } catch (Exception e) {
            log.error("Failed to save notification to Redis", e);
            throw new RuntimeException("Failed to save notification", e);
        }
    }

    private void addNotificationToUser(String userKey, String notificationKey) {
        try {
            redisTemplate.opsForList().leftPush(userKey, notificationKey);
            redisTemplate.expire(userKey, NOTIFICATION_TTL);
            
            Long size = redisTemplate.opsForList().size(userKey);
            if (size != null && size > maxNotificationsPerUser) {
                List<String> excessKeys = redisTemplate.opsForList().range(
                        userKey, maxNotificationsPerUser, -1);
                if (excessKeys != null && !excessKeys.isEmpty()) {
                    for (String oldKey : excessKeys) {
                        redisTemplate.delete(oldKey);
                    }
                }
                redisTemplate.opsForList().trim(userKey, 0, maxNotificationsPerUser - 1);
            }
        } catch (Exception e) {
            log.warn("Failed to add notification to user key: {} (notification still saved to Redis)", userKey, e);
        }
    }

    public boolean existsByCallSidAndEventType(String callSid, String eventType) {
        if (callSid == null || callSid.isBlank()) {
            log.warn("existsByCallSidAndEventType called with null or blank callSid");
            return false;
        }
        
        String safeEventType = eventType != null && !eventType.isBlank() ? eventType : "UNKNOWN";
        
        try {
            String duplicateKey = DUPLICATE_CHECK_KEY_PREFIX + callSid + ":" + safeEventType;
            Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(duplicateKey, "1", DUPLICATE_CHECK_TTL);
            return setIfAbsent == null || !setIfAbsent;
        } catch (Exception e) {
            log.error("Failed to check duplicate notification", e);
            return false;
        }
    }

    public List<CallNotificationResponse> findByUserEmail(String email) {
        if (email == null || email.isBlank()) {
            return new ArrayList<>();
        }
        return findByUserKey(USER_NOTIFICATIONS_KEY_PREFIX + "email:" + email);
    }

    public List<CallNotificationResponse> findByUserPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return new ArrayList<>();
        }
        // Normalize phone number before lookup to match the format in Redis
        String normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(phone);
        return findByUserKey(USER_NOTIFICATIONS_KEY_PREFIX + "phone:" + normalizedPhone);
    }

    private List<CallNotificationResponse> findByUserKey(String userKey) {
        try {
            List<String> notificationKeys = redisTemplate.opsForList().range(userKey, 0, -1);
            if (notificationKeys == null || notificationKeys.isEmpty()) {
                return new ArrayList<>();
            }

            List<CallNotificationResponse> notifications = new ArrayList<>();
            for (String notificationKey : notificationKeys) {
                String notificationJson = redisTemplate.opsForValue().get(notificationKey);
                if (notificationJson != null) {
                    try {
                        CallNotificationResponse notification = objectMapper.readValue(
                                notificationJson, CallNotificationResponse.class);
                        notifications.add(notification);
                    } catch (Exception e) {
                        log.warn("Failed to deserialize notification: {}", notificationKey, e);
                    }
                }
            }
            return notifications;
        } catch (Exception e) {
            log.error("Failed to fetch notifications for user key: {}", userKey, e);
            return new ArrayList<>();
        }
    }
}

