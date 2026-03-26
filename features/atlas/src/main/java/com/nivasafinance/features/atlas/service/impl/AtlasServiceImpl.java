package com.nivasafinance.features.atlas.service.impl;

import com.nivasafinance.common.events.payload.LeadCallLogCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadCallLogUpdateEventPayload;
import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.MessageProvider;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.features.atlas.service.AtlasService;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.AtlasJobStatus;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.service.CallWriteService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AtlasServiceImpl implements AtlasService {

    private static final String LEAD_IDENTIFIER = "leadIdentifier";
    private static final String CALL_LOG_IDENTIFIER = "callLogIdentifier";
    private static final String RECORDING_URL = "recordingUrl";
    private static final String EVENT_TYPE = "eventType";

    private final MessagePublisherFactory messagePublisherFactory;
    private final MessagingProperties messagingProperties;
    private final CallReadService callReadService;
    private final CallWriteService callWriteService;
    private final UserReadService userReadService;
    private final UserRoleService userRoleService;

    public AtlasServiceImpl(
            MessagePublisherFactory messagePublisherFactory,
            MessagingProperties messagingProperties,
            CallReadService callReadService,
            CallWriteService callWriteService,
            UserReadService userReadService,
            UserRoleService userRoleService) {
        this.messagePublisherFactory = messagePublisherFactory;
        this.messagingProperties = messagingProperties;
        this.callReadService = callReadService;
        this.callWriteService = callWriteService;
        this.userReadService = userReadService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void handleLeadCallLogCreated(LeadCallLogCreationEventPayload payload) {
        enqueueAtlasTranscriptionJob(
                payload.getLeadIdentifier(),
                payload.getCallLogIdentifier(),
                null,
                payload.getPrimaryRole());
    }

    @Override
    public void handleLeadCallLogUpdated(LeadCallLogUpdateEventPayload payload) {
        String recordingOverride = StringUtils.hasText(payload.getRecordingUrl()) ? payload.getRecordingUrl().trim() : null;
        enqueueAtlasTranscriptionJob(
                payload.getLeadIdentifier(),
                payload.getCallLogIdentifier(),
                recordingOverride,
                payload.getPrimaryRole());
    }

    /**
     * Enqueues an Atlas transcription job when a recording URL exists and the job is not already
     * {@link AtlasJobStatus#INITIATED} or {@link AtlasJobStatus#PROCESSING}.
     *
     * @param recordingUrlOverride if non-blank, used as recording URL; otherwise read from persisted call log
     */
    private void enqueueAtlasTranscriptionJob(
            UUID leadIdentifier,
            UUID callLogIdentifier,
            String recordingUrlOverride,
            String primaryRoleFallback) {
        if (!isNavigatorAtlasQueueConfigured()) {
            log.warn("Navigator Atlas queue is not configured; skipping transcription job");
            return;
        }
        if (leadIdentifier == null) {
            log.warn("Lead call log event missing leadIdentifier; skipping Atlas job for callLog {}", callLogIdentifier);
            return;
        }
        CallLogResponse callLog = callReadService.getCallLogByIdentifier(callLogIdentifier);
        String recordingUrl = StringUtils.hasText(recordingUrlOverride)
                ? recordingUrlOverride.trim()
                : recordingUrlFromCallLog(callLog);
        if (!StringUtils.hasText(recordingUrl)) {
            return;
        }
        if (shouldSkipForExistingJob(callLog.getAiAnalysis())) {
            log.debug("Skipping Atlas enqueue for callLog {} — job already initiated or in progress", callLogIdentifier);
            return;
        }

        callWriteService.mergeAiAnalysisByIdentifier(
                callLogIdentifier,
                CallLog.AiAnalysisDetails.builder()
                        .status(AtlasJobStatus.INITIATED)
                        .build());

        Map<String, Object> body = new LinkedHashMap<>();
        String primaryRoleForEventType = resolvePrimaryRoleForAtlasEventType(callLog, primaryRoleFallback);
        body.put(EVENT_TYPE, atlasEventTypeForPrimaryRole(primaryRoleForEventType));
        body.put(LEAD_IDENTIFIER, leadIdentifier.toString());
        body.put(CALL_LOG_IDENTIFIER, callLogIdentifier.toString());
        body.put(RECORDING_URL, recordingUrl);
        String messageId = callLogIdentifier.toString();
        try {
            messagePublisherFactory.getPublisher().publish(QueueType.NAVIGATOR_ATLAS, messageId, body);
        } catch (RuntimeException ex) {
            log.error("Atlas transcription enqueue failed for callLog {}", callLogIdentifier, ex);
            callWriteService.mergeAiAnalysisByIdentifier(
                    callLogIdentifier,
                    CallLog.AiAnalysisDetails.builder()
                            .status(AtlasJobStatus.PUBLISHING_FAILED)
                            .build());
        }
    }

    private static String recordingUrlFromCallLog(CallLogResponse callLog) {
        if (callLog.getRecordingDetails() == null || callLog.getRecordingDetails().getUrl() == null) {
            return null;
        }
        String url = callLog.getRecordingDetails().getUrl().trim();
        return url.isEmpty() ? null : url;
    }

    private boolean isNavigatorAtlasQueueConfigured() {
        MessageProvider provider = messagingProperties.getProvider();
        if (provider == MessageProvider.LOCAL) {
            return true;
        }
        if (provider != MessageProvider.SQS) {
            return false;
        }
        Map<QueueType, String> queues = messagingProperties.getSqs().getQueues();
        return queues != null && StringUtils.hasText(queues.get(QueueType.NAVIGATOR_ATLAS));
    }

    /**
     * Inbound: staff leg {@code toNumber}; outbound: staff leg {@code fromNumber}. Resolves role via primary mobile;
     * falls back to {@code payloadFallbackPrimaryRole} (publisher context user).
     */
    private String resolvePrimaryRoleForAtlasEventType(CallLogResponse callLog, String payloadFallbackPrimaryRole) {
        String routingPhone = routingNumberForStaffRoleResolution(callLog);
        if (StringUtils.hasText(routingPhone)) {
            Optional<String> staffUsername = userReadService.resolveUsernameByPhone(routingPhone.trim());
            if (staffUsername.isPresent() && StringUtils.hasText(staffUsername.get())) {
                String role = userRoleService.getPrimaryRoleForUsername(staffUsername.get().trim());
                if (StringUtils.hasText(role)) {
                    return role;
                }
            }
        }
        return StringUtils.hasText(payloadFallbackPrimaryRole) ? payloadFallbackPrimaryRole.trim() : null;
    }

    private static String routingNumberForStaffRoleResolution(CallLogResponse callLog) {
        if (callLog == null || callLog.getDirection() == null) {
            return null;
        }
        if (callLog.getDirection() == CallDirection.INBOUND) {
            return nullIfBlank(callLog.getToNumber());
        }
        if (callLog.getDirection() == CallDirection.OUTBOUND) {
            return nullIfBlank(callLog.getFromNumber());
        }
        return null;
    }

    private static String nullIfBlank(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Atlas queue payload expects only {@code SME} or {@code CSE} values.
     *
     * We intentionally hardcode these strings because downstream Atlas consumers do not understand other
     * role keys; anything non-SME is treated as {@code CSE} to keep behavior stable.
     */
    private static String atlasEventTypeForPrimaryRole(String primaryRole) {
        if (StringUtils.hasText(primaryRole) && "SME".equalsIgnoreCase(primaryRole.trim())) {
            return "SME";
        }
        return "CSE";
    }

    private static boolean shouldSkipForExistingJob(CallLog.AiAnalysisDetails ai) {
        if (ai == null) {
            return false;
        }
        AtlasJobStatus status = ai.getStatus();
        if (status == AtlasJobStatus.INITIATED || status == AtlasJobStatus.PROCESSING) {
            return true;
        }
        if (!StringUtils.hasText(ai.getJobId())) {
            return false;
        }
        return status == null;
    }
}
