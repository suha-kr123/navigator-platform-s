package com.nivasafinance.features.task.listener;

import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadStatusChangeEventPayload;
import com.nivasafinance.features.task.service.TaskWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

/**
 * Listener that handles lead status change events and closes all open tasks for the lead asynchronously.
 * This ensures that task closing doesn't block the lead status change operation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCloseListener {

    private final TaskWriteService taskWriteService;

    /**
     * Handles lead status change events (REJECTED, WITHDRAWN, DROPOFF, COMPLETED) asynchronously
     * to close all open tasks for the lead.
     * The listener only processes after the transaction commits to ensure data consistency.
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            condition = "#event.eventType == 'LEAD_REJECTED' or " +
                       "#event.eventType == 'LEAD_WITHDRAWN' or " +
                       "#event.eventType == 'LEAD_DROPOFF' or " +
                       "#event.eventType == 'LEAD_COMPLETED'"
    )
    @Async
    public void handleLeadStatusChangeEvent(SystemEvent<?> event) {
        if (!(event.getPayload() instanceof LeadStatusChangeEventPayload)) {
            log.warn("Received non-LeadStatusChangeEventPayload for event: {}", event.getEventType());
            return;
        }

        LeadStatusChangeEventPayload payload = (LeadStatusChangeEventPayload) event.getPayload();
        UUID leadIdentifier = payload.getLeadIdentifier();
        String eventType = event.getEventType();

        if (leadIdentifier == null) {
            log.warn("Lead identifier is null in event payload for event: {}. Skipping task closure.", eventType);
            return;
        }

        try {
            String outcome = mapEventTypeToOutcome(eventType);
            log.info("Closing all open tasks for lead {} with outcome {} (triggered by event {})", 
                    leadIdentifier, outcome, eventType);
            
            taskWriteService.closeAllOpenTasksForLead(leadIdentifier, outcome);
            
            log.info("Successfully closed all open tasks for lead {}", leadIdentifier);
        } catch (Exception e) {
            log.error("Failed to close tasks for lead {} (event: {}): {}", 
                    leadIdentifier, eventType, e.getMessage(), e);
        }
    }

    /**
     * Maps the business event type to the corresponding task outcome value.
     */
    private String mapEventTypeToOutcome(String eventType) {
        switch (eventType) {
            case "LEAD_REJECTED":
                return "REJECTED";
            case "LEAD_WITHDRAWN":
                return "WITHDRAWN";
            case "LEAD_DROPOFF":
                return "DROPOFF";
            case "LEAD_COMPLETED":
                return "CLOSED";
            default:
                log.warn("Unknown event type for task closure: {}", eventType);
                return "CLOSED"; // Default fallback
        }
    }
}

