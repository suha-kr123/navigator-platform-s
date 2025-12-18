package com.nivasafinance.webhooks.call.service;

import com.nivasafinance.webhooks.call.dto.CallNotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CallNotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Value("${websocket.topic-prefix:/topic}")
    private String topicPrefix;

    public void sendNotificationToUser(CallNotificationResponse notification, String username) {
        if (username != null && !username.isBlank()) {
            messagingTemplate.convertAndSendToUser(username, "/queue/call-notifications", notification);
        }
    }

    public void sendNotificationToTopic(CallNotificationResponse notification) {
        messagingTemplate.convertAndSend(topicPrefix + "/call-notifications", notification);
    }
}

