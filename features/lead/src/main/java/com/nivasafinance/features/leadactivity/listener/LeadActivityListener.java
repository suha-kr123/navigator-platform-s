package com.nivasafinance.features.leadactivity.listener;

import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.features.leadactivity.factory.LeadActivityDataFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeadActivityListener {

    private final LeadActivityDataFactory leadActivityDataFactory;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleEvent(SystemEvent<?> event) {
        String eventType = event.getEventType();
        log.info("LeadActivityListener -> Received system event: {}", eventType);
        leadActivityDataFactory.recordEvent(eventType, event.getPayload(), event.getUsername());
    }
}


