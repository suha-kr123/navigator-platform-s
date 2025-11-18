package com.nivasafinance.notification.orchestrator.payload;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
@Builder
public class NotificationPayload {

    private final String entityId;

    private final Map<String, Object> attributes;

    public Map<String, Object> getAttributes() {
        return attributes == null ? Collections.emptyMap() : Collections.unmodifiableMap(attributes);
    }
}


