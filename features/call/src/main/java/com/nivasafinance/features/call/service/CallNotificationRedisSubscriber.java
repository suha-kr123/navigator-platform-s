package com.nivasafinance.features.call.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.call.dto.CallNotificationBroadcast;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallNotificationRedisSubscriber implements MessageListener {

    private final CallNotificationSseService sseService;
    private final ObjectMapper objectMapper;
    private final CallNotificationInstanceId instanceId;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody());
            CallNotificationBroadcast broadcast = objectMapper.readValue(
                    json, CallNotificationBroadcast.class);

            if (broadcast.getNotification() == null || broadcast.getUsernames() == null
                    || broadcast.getUsernames().isEmpty()) {
                log.warn("Received invalid broadcast message: missing notification or usernames");
                return;
            }

            // Skip messages published by this instance — it already delivered locally
            if (instanceId.getId().equals(broadcast.getSourceInstanceId())) {
                log.debug("Skipping broadcast from own instance for callSid: {}",
                        broadcast.getNotification().getCallSid());
                return;
            }

            log.info("Received broadcast for callSid: {}, target usernames: {}, from instance: {}",
                    broadcast.getNotification().getCallSid(), broadcast.getUsernames(),
                    broadcast.getSourceInstanceId());

            for (String username : broadcast.getUsernames()) {
                try {
                    sseService.sendNotificationToUser(broadcast.getNotification(), username);
                } catch (Exception e) {
                    log.error("Failed to deliver broadcast notification to user: {}", username, e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to deserialize broadcast notification from Redis: {}",
                    new String(message.getBody()), e);
        }
    }
}
