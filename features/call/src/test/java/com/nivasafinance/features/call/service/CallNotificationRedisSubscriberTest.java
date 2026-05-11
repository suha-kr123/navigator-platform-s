package com.nivasafinance.features.call.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nivasafinance.common.dto.CallNotificationResponse;
import com.nivasafinance.features.call.dto.CallNotificationBroadcast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallNotificationRedisSubscriberTest {

    @Mock
    private CallNotificationSseService sseService;

    private ObjectMapper objectMapper;
    private CallNotificationInstanceId instanceId;
    private CallNotificationRedisSubscriber subscriber;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        instanceId = new CallNotificationInstanceId();
        subscriber = new CallNotificationRedisSubscriber(sseService, objectMapper, instanceId);
    }

    @Test
    void onMessage_validBroadcast_deliversToAllUsernames() throws Exception {
        CallNotificationResponse notification = CallNotificationResponse.builder()
                .callSid("sid-1")
                .callFrom("9876543210")
                .callTo("1234567890")
                .build();

        CallNotificationBroadcast broadcast = CallNotificationBroadcast.builder()
                .notification(notification)
                .usernames(List.of("alice", "bob"))
                .sourceInstanceId("other-instance")
                .build();

        String json = objectMapper.writeValueAsString(broadcast);
        Message message = new DefaultMessage("channel".getBytes(), json.getBytes());

        subscriber.onMessage(message, null);

        verify(sseService).sendNotificationToUser(notification, "alice");
        verify(sseService).sendNotificationToUser(notification, "bob");
    }

    @Test
    void onMessage_fromOwnInstance_skipsDelivery() throws Exception {
        CallNotificationResponse notification = CallNotificationResponse.builder()
                .callSid("sid-1")
                .build();

        CallNotificationBroadcast broadcast = CallNotificationBroadcast.builder()
                .notification(notification)
                .usernames(List.of("alice"))
                .sourceInstanceId(instanceId.getId())
                .build();

        String json = objectMapper.writeValueAsString(broadcast);
        Message message = new DefaultMessage("channel".getBytes(), json.getBytes());

        subscriber.onMessage(message, null);

        verifyNoInteractions(sseService);
    }

    @Test
    void onMessage_malformedJson_doesNotThrow() {
        Message message = new DefaultMessage("channel".getBytes(), "not-json".getBytes());

        subscriber.onMessage(message, null);

        verifyNoInteractions(sseService);
    }

    @Test
    void onMessage_emptyUsernames_skipsDelivery() throws Exception {
        CallNotificationBroadcast broadcast = CallNotificationBroadcast.builder()
                .notification(CallNotificationResponse.builder().callSid("sid-2").build())
                .usernames(List.of())
                .sourceInstanceId("other-instance")
                .build();

        String json = objectMapper.writeValueAsString(broadcast);
        Message message = new DefaultMessage("channel".getBytes(), json.getBytes());

        subscriber.onMessage(message, null);

        verifyNoInteractions(sseService);
    }

    @Test
    void onMessage_sseServiceThrows_continuesWithOtherUsers() throws Exception {
        CallNotificationResponse notification = CallNotificationResponse.builder()
                .callSid("sid-3")
                .build();

        CallNotificationBroadcast broadcast = CallNotificationBroadcast.builder()
                .notification(notification)
                .usernames(List.of("alice", "bob"))
                .sourceInstanceId("other-instance")
                .build();

        String json = objectMapper.writeValueAsString(broadcast);
        Message message = new DefaultMessage("channel".getBytes(), json.getBytes());

        doThrow(new RuntimeException("SSE error")).when(sseService)
                .sendNotificationToUser(notification, "alice");

        subscriber.onMessage(message, null);

        verify(sseService).sendNotificationToUser(notification, "alice");
        verify(sseService).sendNotificationToUser(notification, "bob");
    }
}
