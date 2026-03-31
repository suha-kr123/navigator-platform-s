package com.nivasafinance.features.lead.listener;

import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadCallLogCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadCallLogUpdateEventPayload;
import com.nivasafinance.features.lead.service.LeadCallReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Recomputes {@code Lead.callSummaryDetails} after call logs linked to a lead change.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeadCallSummaryListener {

    private final LeadCallReadService leadCallReadService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            condition = "#event.eventType == 'LEAD_CALL_LOG_CREATED'"
    )
    @Async
    public void onLeadCallLogCreated(SystemEvent<?> event) {
        handlePayload(event.getPayload(), BusinessEvent.LEAD_CALL_LOG_CREATED.name());
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            condition = "#event.eventType == 'LEAD_CALL_LOG_UPDATED'"
    )
    @Async
    public void onLeadCallLogUpdated(SystemEvent<?> event) {
        handlePayload(event.getPayload(), BusinessEvent.LEAD_CALL_LOG_UPDATED.name());
    }

    private void handlePayload(Object payload, String eventLabel) {
        Long leadId = extractLeadId(payload);
        if (leadId == null) {
            log.warn("{} missing leadId in payload; skipping call summary refresh", eventLabel);
            return;
        }
        try {
            leadCallReadService.recalculateLeadCallSummary(leadId);
        } catch (Exception ex) {
            log.error("Failed to refresh lead call summary for leadId {} after {}", leadId, eventLabel, ex);
        }
    }

    private static Long extractLeadId(Object payload) {
        if (payload instanceof LeadCallLogCreationEventPayload p) {
            return p.getLeadId();
        }
        if (payload instanceof LeadCallLogUpdateEventPayload p) {
            return p.getLeadId();
        }
        return null;
    }
}
