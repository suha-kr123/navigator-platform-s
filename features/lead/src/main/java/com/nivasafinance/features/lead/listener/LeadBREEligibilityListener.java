package com.nivasafinance.features.lead.listener;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.StageTransitionEventPayload;
import com.nivasafinance.features.lead.service.LeadEligibilityWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeadBREEligibilityListener {

    private final LeadEligibilityWriteService leadEligibilityWriteService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            condition = "#event.eventType == 'STAGE_TRANSITIONED'"
    )
    @Async
    public void onStageTransitioned(SystemEvent<?> event) {
        if (!(event.getPayload() instanceof StageTransitionEventPayload payload)) {
            return;
        }
        if (payload.getEntityType() != EntityType.LEAD) {
            return;
        }
        try {
            leadEligibilityWriteService.executeEligibilityOnStageTransition(payload.getEntityIdentifier());
        } catch (Exception e) {
            log.error("Failed to execute eligibility BRE for lead {}", payload.getEntityIdentifier(), e);
        }
    }
}
