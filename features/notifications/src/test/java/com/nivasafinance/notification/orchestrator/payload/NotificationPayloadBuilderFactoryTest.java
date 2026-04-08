package com.nivasafinance.notification.orchestrator.payload;

import com.nivasafinance.common.events.BusinessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPayloadBuilderFactoryTest {

    private NotificationPayloadBuilderFactory factory;

    private NotificationPayloadBuilder leadCreatedBuilder;
    private NotificationPayloadBuilder leadUpdatedBuilder;

    @BeforeEach
    void setUp() {
        leadCreatedBuilder = mock(NotificationPayloadBuilder.class);
        leadUpdatedBuilder = mock(NotificationPayloadBuilder.class);

        lenient().when(leadCreatedBuilder.supports(BusinessEvent.LEAD_CREATED)).thenReturn(true);
        lenient().when(leadCreatedBuilder.supports(BusinessEvent.LEAD_UPDATED)).thenReturn(false);
        lenient().when(leadUpdatedBuilder.supports(BusinessEvent.LEAD_CREATED)).thenReturn(false);
        lenient().when(leadUpdatedBuilder.supports(BusinessEvent.LEAD_UPDATED)).thenReturn(true);

        factory = new NotificationPayloadBuilderFactory(List.of(leadCreatedBuilder, leadUpdatedBuilder));
    }

    @Test
    void build_supportedEvent_delegatesToCorrectBuilder() {
        Map<String, Object> expectedPayload = Map.of("leadId", "123");
        when(leadCreatedBuilder.build("payload")).thenReturn(expectedPayload);

        Map<String, Object> result = factory.build(BusinessEvent.LEAD_CREATED, "payload");

        assertEquals(expectedPayload, result, "Should return payload from matching builder");
        verify(leadCreatedBuilder).build("payload");
    }

    @Test
    void build_secondBuilder_delegatesCorrectly() {
        Map<String, Object> expectedPayload = Map.of("leadId", "456");
        when(leadUpdatedBuilder.build("payload2")).thenReturn(expectedPayload);

        Map<String, Object> result = factory.build(BusinessEvent.LEAD_UPDATED, "payload2");

        assertEquals(expectedPayload, result, "Should return payload from second builder");
        verify(leadUpdatedBuilder).build("payload2");
    }

    @Test
    void build_unsupportedEvent_throwsException() {
        assertThrows(IllegalStateException.class,
                () -> factory.build(BusinessEvent.LEAD_REJECTED, "payload"),
                "Should throw for unsupported event type");
    }

    @Test
    void build_emptyBuilderList_throwsException() {
        NotificationPayloadBuilderFactory emptyFactory = new NotificationPayloadBuilderFactory(List.of());

        assertThrows(IllegalStateException.class,
                () -> emptyFactory.build(BusinessEvent.LEAD_CREATED, "payload"),
                "Should throw when no builders registered");
    }
}
