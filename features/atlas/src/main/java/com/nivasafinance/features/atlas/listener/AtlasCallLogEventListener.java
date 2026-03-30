package com.nivasafinance.features.atlas.listener;

import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadCallLogCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadCallLogUpdateEventPayload;
import com.nivasafinance.features.atlas.service.AtlasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

/**
 * Triggers Atlas transcription for {@code LEAD_CALL_LOG_CREATED} and {@code LEAD_CALL_LOG_UPDATED}
 * when a recording URL is available and the job is not already initiated.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AtlasCallLogEventListener {

    private final AtlasService atlasService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            condition = "#event.eventType == 'LEAD_CALL_LOG_CREATED'"
    )
    @Async
    public void handleLeadCallLogCreated(SystemEvent<?> event) {
        if (!(event.getPayload() instanceof LeadCallLogCreationEventPayload payload)) {
            log.warn("LEAD_CALL_LOG_CREATED with unexpected payload type: {}", event.getPayload());
            return;
        }
        try {
            atlasService.handleLeadCallLogCreated(payload);
        } catch (Exception ex) {
            log.error("Atlas transcription trigger failed for created callLog {}", payload.getCallLogIdentifier(), ex);
        }
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            condition = "#event.eventType == 'LEAD_CALL_LOG_UPDATED'"
    )
    @Async
    public void handleLeadCallLogUpdated(SystemEvent<?> event) {
        if (!(event.getPayload() instanceof LeadCallLogUpdateEventPayload payload)) {
            log.warn("LEAD_CALL_LOG_UPDATED with unexpected payload type: {}", event.getPayload());
            return;
        }
        if (!StringUtils.hasText(payload.getRecordingUrl())) {
            log.debug("LEAD_CALL_LOG_UPDATED without recording URL; skipping Atlas for callLog {}",
                    payload.getCallLogIdentifier());
            return;
        }
        try {
            atlasService.handleLeadCallLogUpdated(payload);
        } catch (Exception ex) {
            log.error("Atlas transcription trigger failed for callLog {}", payload.getCallLogIdentifier(), ex);
        }
    }
}
