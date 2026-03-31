package com.nivasafinance.externals.exotel.service.impl;

import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadCallLogUpdateEventPayload;
import com.nivasafinance.features.call.dto.ReconciliationCorrectionRecord;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.service.CallReconciliationLogWriteService;
import com.nivasafinance.features.call.service.CallWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processes a single call log reconciliation row in its own transaction.
 * Separated from ExotelReconciliationServiceImpl so that Spring proxy-based
 * {@link Transactional} works correctly (avoids self-invocation issue).
 * Each row gets its own transaction so events are delivered individually via
 * {@link org.springframework.transaction.event.TransactionalEventListener}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class ReconciliationRowProcessor {

    private static final String LOG_CORRECTION =
            "Exotel reconciliation correction: call_log_id={}, provider_call_sid={}, "
                    + "old_status={}, new_status={}, old_duration={}, new_duration={}, correction_source={}";

    private final CallReadService callReadService;
    private final CallWriteService callWriteService;
    private final CallReconciliationLogWriteService callReconciliationLogWriteService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReconcileResult processRow(CallLog callLog, CallStatus exotelStatus, Long exotelDuration,
                                      String correctionSource) {
        CallStatus oldStatus = callLog.getStatus();
        boolean statusDiffers = exotelStatus != null && exotelStatus != oldStatus;

        Long dbDuration = callLog.getCompletionDetails() != null
                ? callLog.getCompletionDetails().getDuration()
                : null;

        boolean durationShouldUpdate = false;
        if (exotelDuration != null && exotelDuration > 0) {
            if (dbDuration == null) {
                durationShouldUpdate = true;
            } else if (!dbDuration.equals(exotelDuration)) {
                durationShouldUpdate = true;
            }
        }

        if (!statusDiffers && !durationShouldUpdate) {
            return ReconcileResult.noChange();
        }

        CallStatus newStatus = statusDiffers ? exotelStatus : oldStatus;
        Long oldDurationForLog = dbDuration;
        Long newDurationForLog = durationShouldUpdate ? exotelDuration : dbDuration;

        CallLog.CompletionDetails updatedCompletionDetails = callLog.getCompletionDetails();
        if (durationShouldUpdate) {
            CallLog.CompletionDetails cur = callLog.getCompletionDetails();
            updatedCompletionDetails = CallLog.CompletionDetails.builder()
                    .duration(exotelDuration)
                    .startTime(cur != null ? cur.getStartTime() : null)
                    .endTime(cur != null ? cur.getEndTime() : null)
                    .legs(cur != null ? cur.getLegs() : null)
                    .build();
        }

        log.info(LOG_CORRECTION, callLog.getId(), callLog.getProviderId(),
                oldStatus, newStatus, oldDurationForLog, newDurationForLog, correctionSource);

        callWriteService.updateCallLogByProviderId(callLog.getProviderId(), UpdateCallLog.builder()
                .status(newStatus)
                .recordingDetails(callLog.getRecordingDetails())
                .completionDetails(updatedCompletionDetails)
                .build());

        Map<String, Object> changes = new LinkedHashMap<>();
        if (statusDiffers) {
            Map<String, Object> statusChange = new LinkedHashMap<>();
            statusChange.put("old", oldStatus != null ? oldStatus.name() : null);
            statusChange.put("new", newStatus != null ? newStatus.name() : null);
            changes.put("status", statusChange);
        }
        if (durationShouldUpdate) {
            Map<String, Object> durationChange = new LinkedHashMap<>();
            durationChange.put("old", oldDurationForLog != null ? oldDurationForLog : 0);
            durationChange.put("new", newDurationForLog != null ? newDurationForLog : 0);
            changes.put("duration", durationChange);
        }

        callReconciliationLogWriteService.recordCorrection(
                new ReconciliationCorrectionRecord(
                        callLog.getId(),
                        callLog.getProviderId(),
                        changes,
                        correctionSource));

        publishLeadCallLogUpdatedEvent(callLog, correctionSource);

        return ReconcileResult.changed(statusDiffers, durationShouldUpdate);
    }

    private void publishLeadCallLogUpdatedEvent(CallLog callLog, String correctionSource) {
        try {
            callReadService.findLeadIdByCallLogId(callLog.getId()).ifPresent(leadId -> {
                String recordingUrl = callLog.getRecordingDetails() != null
                        ? callLog.getRecordingDetails().getUrl() : null;

                LeadCallLogUpdateEventPayload payload = LeadCallLogUpdateEventPayload.builder()
                        .callLogId(callLog.getId())
                        .callLogIdentifier(callLog.getIdentifier())
                        .leadId(leadId)
                        .recordingUrl(recordingUrl)
                        .primaryRole(null)
                        .build();

                applicationEventPublisher.publishEvent(
                        new SystemEvent<>(
                                BusinessEvent.LEAD_CALL_LOG_UPDATED.toString(),
                                payload,
                                correctionSource));

                log.info("Published LEAD_CALL_LOG_UPDATED event for callLogId={}, leadId={}",
                        callLog.getId(), leadId);
            });
        } catch (Exception e) {
            log.warn("Failed to publish LEAD_CALL_LOG_UPDATED event for callLogId={}: {}",
                    callLog.getId(), e.getMessage());
        }
    }

    static final class ReconcileResult {
        final boolean updated;
        final boolean statusChanged;
        final boolean durationChanged;

        private ReconcileResult(boolean updated, boolean statusChanged, boolean durationChanged) {
            this.updated = updated;
            this.statusChanged = statusChanged;
            this.durationChanged = durationChanged;
        }

        static ReconcileResult noChange() {
            return new ReconcileResult(false, false, false);
        }

        static ReconcileResult changed(boolean statusChanged, boolean durationChanged) {
            return new ReconcileResult(true, statusChanged, durationChanged);
        }
    }
}
