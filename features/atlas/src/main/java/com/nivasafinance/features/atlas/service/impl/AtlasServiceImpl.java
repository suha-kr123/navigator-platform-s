package com.nivasafinance.features.atlas.service.impl;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.events.payload.LeadCallLogCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadCallLogUpdateEventPayload;
import com.nivasafinance.common.events.payload.StageTransitionEventPayload;
import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.MessageProvider;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.features.atlas.service.AtlasService;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.AtlasJobStatus;
import com.nivasafinance.features.call.entity.CallLogLead;
import com.nivasafinance.features.call.repository.CallLogLeadRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.service.CallWriteService;
import com.nivasafinance.features.dataprovider.service.DataProviderExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AtlasServiceImpl implements AtlasService {

    /**
     * Data provider {@value #ATLAS_LEAD_STAGE_DATA_PROVIDER_NAME} must return {@code stage_eligible} (boolean) and
     * {@code primary_role}. Example:
     * <pre>{@code
     * SELECT
     *     (((l.workflow_details->'currentStageDetails')->>'stageKey') = 'Expert Screening') AS stage_eligible,
     *     (SELECT urm.role FROM n_user_role_mapping urm
     *      WHERE urm.username = (l.workflow_details->'currentStageDetails')->>'assignedTo'
     *        AND urm.is_primary = true LIMIT 1) AS primary_role
     * FROM n_lead l
     * WHERE l.lead_identifier = :leadIdentifier
     * }</pre>
     */
    private static final String ATLAS_LEAD_STAGE_DATA_PROVIDER_NAME = "atlas_lead_stage";

    /** Bind parameter name for {@link #ATLAS_LEAD_STAGE_DATA_PROVIDER_NAME} SQL. */
    private static final String LEAD_IDENTIFIER_PARAM = "leadIdentifier";

    /** Whether the lead's stage allows Atlas enqueue; from data provider SQL. */
    private static final String STAGE_ELIGIBLE_COLUMN = "stage_eligible";

    /** Primary role for Atlas routing; from stage assignee via {@code n_user_role_mapping}. */
    private static final String PRIMARY_ROLE_COLUMN = "primary_role";

    private static final String LEAD_IDENTIFIER = "leadIdentifier";
    private static final String CALL_LOG_IDENTIFIER = "callLogIdentifier";
    private static final String RECORDING_URL = "recordingUrl";
    private static final String EVENT_TYPE = "eventType";

    private final MessagePublisherFactory messagePublisherFactory;
    private final MessagingProperties messagingProperties;
    private final CallReadService callReadService;
    private final CallWriteService callWriteService;
    private final DataProviderExecutor dataProviderExecutor;
    private final CallLogLeadRepositoryWrapper callLogLeadRepositoryWrapper;

    public AtlasServiceImpl(
            MessagePublisherFactory messagePublisherFactory,
            MessagingProperties messagingProperties,
            CallReadService callReadService,
            CallWriteService callWriteService,
            DataProviderExecutor dataProviderExecutor,
            CallLogLeadRepositoryWrapper callLogLeadRepositoryWrapper) {
        this.messagePublisherFactory = messagePublisherFactory;
        this.messagingProperties = messagingProperties;
        this.callReadService = callReadService;
        this.callWriteService = callWriteService;
        this.dataProviderExecutor = dataProviderExecutor;
        this.callLogLeadRepositoryWrapper = callLogLeadRepositoryWrapper;
    }

    @Override
    public void handleLeadCallLogCreated(LeadCallLogCreationEventPayload payload) {
        enqueueAtlasTranscriptionJob(
                payload.getLeadIdentifier(),
                payload.getCallLogIdentifier(),
                null);
    }

    @Override
    public void handleLeadCallLogUpdated(LeadCallLogUpdateEventPayload payload) {
        String recordingOverride = StringUtils.hasText(payload.getRecordingUrl()) ? payload.getRecordingUrl().trim() : null;
        enqueueAtlasTranscriptionJob(
                payload.getLeadIdentifier(),
                payload.getCallLogIdentifier(),
                recordingOverride);
    }

    @Override
    public void handleLeadStageTransitioned(StageTransitionEventPayload payload) {
        if (payload.getEntityType() != EntityType.LEAD) {
            return;
        }
        UUID leadIdentifier = payload.getEntityIdentifier();
        Long leadId = payload.getEntityId();
        if (leadIdentifier == null || leadId == null) {
            log.debug("Skipping Atlas on stage transition — missing lead identifier or lead id");
            return;
        }
        List<CallLogLead> links = callLogLeadRepositoryWrapper.findAllByLeadIdOrderByCallLogIdDesc(leadId);
        if (links.isEmpty()) {
            return;
        }
        List<Long> callLogIds = links.stream().map(CallLogLead::getCallLogId).collect(Collectors.toList());
        List<CallLogResponse> callLogs = callReadService.getCallLogsByIDs(callLogIds);
        for (CallLogResponse callLog : callLogs) {
            if (callLog == null || callLog.getIdentifier() == null) {
                continue;
            }
            enqueueAtlasTranscriptionJob(leadIdentifier, callLog.getIdentifier(), null);
        }
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
            String recordingUrlOverride) {
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

        Map<String, String> atlasLeadRow = loadAtlasLeadDataProviderRow(leadIdentifier, callLogIdentifier);
        if (atlasLeadRow == null) {
            return;
        }
        if (!isStageEligibleFromProvider(atlasLeadRow)) {
            log.debug(
                    "Skipping Atlas enqueue for callLog {} — stage_eligible is false or missing for lead {}",
                    callLogIdentifier,
                    leadIdentifier);
            return;
        }

        String primaryRoleForEventType = atlasLeadRow.get(PRIMARY_ROLE_COLUMN);
        if (!StringUtils.hasText(primaryRoleForEventType)) {
            log.debug(
                    "Skipping Atlas enqueue for callLog {} — no primary_role from data provider for lead {}",
                    callLogIdentifier,
                    leadIdentifier);
            return;
        }
        if ("SME".equals(atlasEventTypeForPrimaryRole(primaryRoleForEventType))) {
            log.debug("Skipping Atlas enqueue for callLog {} — SME recordings are not sent to Atlas", callLogIdentifier);
            return;
        }

        callWriteService.mergeAiAnalysisByIdentifier(
                callLogIdentifier,
                CallLog.AiAnalysisDetails.builder()
                        .status(AtlasJobStatus.INITIATED)
                        .build());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(EVENT_TYPE, atlasEventTypeForPrimaryRole(primaryRoleForEventType.trim()));
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

    /**
     * Interprets {@value #STAGE_ELIGIBLE_COLUMN} from JDBC (often {@code "true"}/{@code "false"} string).
     */
    private static boolean isStageEligibleFromProvider(Map<String, String> row) {
        if (row == null) {
            return false;
        }
        String raw = row.get(STAGE_ELIGIBLE_COLUMN);
        if (!StringUtils.hasText(raw)) {
            return false;
        }
        String v = raw.trim();
        return "true".equalsIgnoreCase(v) || "t".equalsIgnoreCase(v) || "1".equals(v);
    }

    private Map<String, String> loadAtlasLeadDataProviderRow(UUID leadIdentifier, UUID callLogIdentifier) {
        try {
            return dataProviderExecutor.executeDataProvider(
                    ATLAS_LEAD_STAGE_DATA_PROVIDER_NAME,
                    Map.of(LEAD_IDENTIFIER_PARAM, leadIdentifier));
        } catch (IllegalArgumentException ex) {
            log.warn(
                    "Atlas lead stage data provider '{}' missing or invalid; skipping transcription for callLog {}",
                    ATLAS_LEAD_STAGE_DATA_PROVIDER_NAME,
                    callLogIdentifier,
                    ex);
            return null;
        }
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
