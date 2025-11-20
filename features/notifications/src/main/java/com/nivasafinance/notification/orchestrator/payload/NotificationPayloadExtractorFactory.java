package com.nivasafinance.notification.orchestrator.payload;

import com.nivasafinance.common.events.BusinessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationPayloadExtractorFactory {

    private final List<NotificationPayloadExtractor> extractors;

    public NotificationPayload extract(BusinessEvent eventType, Object eventPayload) {
        return extractors.stream()
                .filter(extractor -> extractor.supports(eventType))
                .findFirst()
                .map(extractor -> extractor.extract(eventType, eventPayload))
                .orElseThrow(() -> new IllegalStateException("No payload extractor registered for event " + eventType));
    }
}


