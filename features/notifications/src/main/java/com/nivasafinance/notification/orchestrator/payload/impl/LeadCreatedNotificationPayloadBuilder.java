package com.nivasafinance.notification.orchestrator.payload.impl;

import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.payload.LeadCreationEventPayload;
import com.nivasafinance.notification.orchestrator.payload.NotificationPayloadBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for LEAD_CREATED event notification payloads.
 */
@Component
public class LeadCreatedNotificationPayloadBuilder implements NotificationPayloadBuilder {

    @Override
    public boolean supports(BusinessEvent eventType) {
        return eventType == BusinessEvent.LEAD_CREATED;
    }

    @Override
    public Map<String, Object> build(Object eventPayload) {
        if (!(eventPayload instanceof LeadCreationEventPayload payload)) {
            throw new IllegalStateException("LeadCreatedNotificationPayloadBuilder expects LeadCreationEventPayload");
        }

        String leadId = payload.getLeadId() != null ? payload.getLeadId().toString() : null;
        if (leadId == null || leadId.isBlank()) {
            throw new IllegalStateException("LeadCreatedNotificationPayloadBuilder requires leadId in payload");
        }

        Map<String, Object> notificationPayload = new HashMap<>();
        notificationPayload.put("leadId", leadId);

        return notificationPayload;
    }
}

