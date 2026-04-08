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
class NotificationPayloadExtractorFactoryTest {

    private NotificationPayloadExtractorFactory factory;

    private NotificationPayloadExtractor leadCreatedExtractor;
    private NotificationPayloadExtractor leadUpdatedExtractor;

    @BeforeEach
    void setUp() {
        leadCreatedExtractor = mock(NotificationPayloadExtractor.class);
        leadUpdatedExtractor = mock(NotificationPayloadExtractor.class);

        lenient().when(leadCreatedExtractor.supports(BusinessEvent.LEAD_CREATED)).thenReturn(true);
        lenient().when(leadCreatedExtractor.supports(BusinessEvent.LEAD_UPDATED)).thenReturn(false);
        lenient().when(leadUpdatedExtractor.supports(BusinessEvent.LEAD_CREATED)).thenReturn(false);
        lenient().when(leadUpdatedExtractor.supports(BusinessEvent.LEAD_UPDATED)).thenReturn(true);

        factory = new NotificationPayloadExtractorFactory(List.of(leadCreatedExtractor, leadUpdatedExtractor));
    }

    @Test
    void extract_supportedEvent_delegatesToCorrectExtractor() {
        NotificationPayload expectedPayload = NotificationPayload.builder()
                .entityId("123").attributes(Map.of("key", "value")).build();
        when(leadCreatedExtractor.extract(BusinessEvent.LEAD_CREATED, "eventPayload"))
                .thenReturn(expectedPayload);

        NotificationPayload result = factory.extract(BusinessEvent.LEAD_CREATED, "eventPayload");

        assertEquals(expectedPayload, result, "Should return payload from matching extractor");
        assertEquals("123", result.getEntityId(), "Entity ID should match");
    }

    @Test
    void extract_secondExtractor_delegatesCorrectly() {
        NotificationPayload expectedPayload = NotificationPayload.builder()
                .entityId("456").build();
        when(leadUpdatedExtractor.extract(BusinessEvent.LEAD_UPDATED, "eventPayload2"))
                .thenReturn(expectedPayload);

        NotificationPayload result = factory.extract(BusinessEvent.LEAD_UPDATED, "eventPayload2");

        assertEquals("456", result.getEntityId(), "Entity ID should match from second extractor");
    }

    @Test
    void extract_unsupportedEvent_throwsException() {
        assertThrows(IllegalStateException.class,
                () -> factory.extract(BusinessEvent.LEAD_REJECTED, "payload"),
                "Should throw for unsupported event type");
    }

    @Test
    void extract_emptyExtractorList_throwsException() {
        NotificationPayloadExtractorFactory emptyFactory = new NotificationPayloadExtractorFactory(List.of());

        assertThrows(IllegalStateException.class,
                () -> emptyFactory.extract(BusinessEvent.LEAD_CREATED, "payload"),
                "Should throw when no extractors registered");
    }
}
