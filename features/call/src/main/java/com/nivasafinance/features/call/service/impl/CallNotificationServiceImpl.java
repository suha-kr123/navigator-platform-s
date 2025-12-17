package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.webhooks.call.dto.CallNotificationResponse;
import com.nivasafinance.webhooks.call.repository.CallNotificationRedisRepository;
import com.nivasafinance.features.call.service.CallNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallNotificationServiceImpl implements CallNotificationService {

    private final CallNotificationRedisRepository notificationRepository;
    private final UserReadService userReadService;

    @Value("${call.notification.max-per-user:5}")
    private int maxNotificationsPerUser;

    @Override
    public List<CallNotificationResponse> getNotificationsForCurrentUser() {
        String username = UserContext.getUsername();
        if (username == null || username.isBlank()) {
            return new ArrayList<>();
        }

        Set<String> seenCallSids = new HashSet<>();
        List<CallNotificationResponse> allNotifications = new ArrayList<>();

        userReadService.findUserByUsername(username).ifPresent(user -> {
            if (user.getPerson() != null && user.getPerson().getMobileNumbers() != null) {
                for (MobileNumberDetails mobile : user.getPerson().getMobileNumbers()) {
                    if (mobile.getNumber() != null) {
                        List<CallNotificationResponse> phoneNotifications = 
                                notificationRepository.findByUserPhone(mobile.getNumber());
                        for (CallNotificationResponse notification : phoneNotifications) {
                            if (notification == null || notification.getCallSid() == null) {
                                continue;
                            }
                            String eventType = notification.getEventType() != null ? notification.getEventType() : "UNKNOWN";
                            String uniqueKey = notification.getCallSid() + ":" + eventType;
                            if (!seenCallSids.contains(uniqueKey)) {
                                allNotifications.add(notification);
                                seenCallSids.add(uniqueKey);
                            }
                        }
                    }
                }
            }
        });

        List<CallNotificationResponse> emailNotifications = notificationRepository.findByUserEmail(username);
        for (CallNotificationResponse notification : emailNotifications) {
            if (notification == null || notification.getCallSid() == null) {
                continue;
            }
            String eventType = notification.getEventType() != null ? notification.getEventType() : "UNKNOWN";
            String uniqueKey = notification.getCallSid() + ":" + eventType;
            if (!seenCallSids.contains(uniqueKey)) {
                allNotifications.add(notification);
                seenCallSids.add(uniqueKey);
            }
        }

        return allNotifications.stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(maxNotificationsPerUser)
                .collect(Collectors.toList());
    }
}

