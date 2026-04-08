package com.nivasafinance.notification.orchestrator.payload.impl;

import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.payload.LeadCreationEventPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LeadCreatedNotificationPayloadBuilderTest {

    private LeadCreatedNotificationPayloadBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new LeadCreatedNotificationPayloadBuilder();
    }

    @Test
    void supports_leadCreated_returnsTrue() {
        assertTrue(builder.supports(BusinessEvent.LEAD_CREATED), "Should support LEAD_CREATED event");
    }

    @Test
    void supports_otherEvent_returnsFalse() {
        assertFalse(builder.supports(BusinessEvent.LEAD_UPDATED), "Should not support LEAD_UPDATED event");
    }

    @Test
    void supports_advisorEvent_returnsFalse() {
        assertFalse(builder.supports(BusinessEvent.ADVISOR_CREATED), "Should not support ADVISOR_CREATED event");
    }

    @Test
    void build_validPayload_returnsMapWithLeadId() {
        UUID leadId = UUID.randomUUID();
        LeadCreationEventPayload payload = LeadCreationEventPayload.builder()
                .id(1L).leadId(leadId).mobileNumber("9876543210").build();

        Map<String, Object> result = builder.build(payload);

        assertNotNull(result, "Should return a non-null map");
        assertEquals(leadId.toString(), result.get("leadId"), "Lead ID should match");
    }

    @Test
    void build_wrongPayloadType_throwsException() {
        assertThrows(IllegalStateException.class,
                () -> builder.build("not a LeadCreationEventPayload"),
                "Should throw for wrong payload type");
    }

    @Test
    void build_nullLeadId_throwsException() {
        LeadCreationEventPayload payload = LeadCreationEventPayload.builder()
                .id(1L).leadId(null).mobileNumber("9876543210").build();

        assertThrows(IllegalStateException.class,
                () -> builder.build(payload),
                "Should throw when leadId is null");
    }

    @Test
    void build_validPayload_onlyContainsLeadId() {
        UUID leadId = UUID.randomUUID();
        LeadCreationEventPayload payload = LeadCreationEventPayload.builder()
                .id(1L).leadId(leadId).mobileNumber("9876543210").build();

        Map<String, Object> result = builder.build(payload);

        assertEquals(1, result.size(), "Should only contain leadId key");
        assertTrue(result.containsKey("leadId"), "Should contain 'leadId' key");
    }
}
