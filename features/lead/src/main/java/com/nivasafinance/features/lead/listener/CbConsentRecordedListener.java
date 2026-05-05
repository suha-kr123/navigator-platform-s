package com.nivasafinance.features.lead.listener;

import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadCbConsentRecordedEventPayload;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.lead.service.LeadCreditBureauWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CbConsentRecordedListener {

    private final LeadCreditBureauWriteService leadCreditBureauWriteService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            condition = "#event.eventType == 'LEAD_CB_CONSENT_RECORDED'"
    )
    @Async("eventTaskExecutor")
    @Retryable(
            retryFor = Exception.class,
            noRetryFor = { BadRequestException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void handleCbConsentRecorded(SystemEvent<?> event) {
        Object payload = event.getPayload();
        if (!(payload instanceof LeadCbConsentRecordedEventPayload cbPayload)) {
            log.warn("CbConsentRecordedListener received event with unexpected payload type: {}",
                    payload != null ? payload.getClass() : null);
            return;
        }
        log.info("Handling LEAD_CB_CONSENT_RECORDED for lead: {}, contact: {}",
                cbPayload.getLeadIdentifier(), cbPayload.getContactIdentifier());
        leadCreditBureauWriteService.initiateEnquiry(
                cbPayload.getLeadIdentifier(), cbPayload.getContactIdentifier());
    }

    @Recover
    public void recoverFromCbEnquiryFailure(Exception e, SystemEvent<?> event) {
        Object payload = event.getPayload();
        if (payload instanceof LeadCbConsentRecordedEventPayload cbPayload) {
            log.error("All retries exhausted for CB enquiry initiation. lead: {}, contact: {}",
                    cbPayload.getLeadIdentifier(), cbPayload.getContactIdentifier(), e);
        } else {
            log.error("All retries exhausted for CB enquiry initiation", e);
        }
    }
}
