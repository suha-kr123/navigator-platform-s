package com.nivasafinance.features.advisoractivity.listener;

import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.features.advisoractivity.factory.AdvisorActivityDataFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdvisorActivityListener {

    private final AdvisorActivityDataFactory advisorActivityDataFactory;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleEvent(SystemEvent<?> event) {
        String eventType = event.getEventType();
        log.info("AdvisorActivityListener -> Received system event: {}", eventType);
        advisorActivityDataFactory.recordEvent(eventType, event.getPayload(), event.getUsername());
    }
}

