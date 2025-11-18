package com.nivasafinance.notification.orchestrator.payload;

import com.nivasafinance.common.events.BusinessEvent;

public interface NotificationPayloadExtractor {

    boolean supports(BusinessEvent eventType);

    NotificationPayload extract(BusinessEvent eventType, Object eventPayload);
}


