package com.nivasafinance.features.lead.listener;

import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadTaskTimelineRefreshPayload;
import com.nivasafinance.features.lead.service.LeadTaskTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeadTaskTimelineListener {

    private final LeadTaskTimelineService leadTaskTimelineService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            condition = "#event.eventType == 'LEAD_TASK_TIMELINE_REFRESH'"
    )
    @Async
    public void onLeadTaskTimelineRefresh(SystemEvent<?> event) {
        Object payload = event.getPayload();
        if (!(payload instanceof LeadTaskTimelineRefreshPayload p) || p.getLeadIdentifier() == null) {
            log.warn("{} missing leadIdentifier in payload; skipping task timeline refresh", BusinessEvent.LEAD_TASK_TIMELINE_REFRESH);
            return;
        }
        try {
            leadTaskTimelineService.refreshByLeadIdentifier(p.getLeadIdentifier());
        } catch (Exception e) {
            log.warn("Failed to refresh task timeline for lead {}: {}", p.getLeadIdentifier(), e.getMessage());
        }
    }
}
