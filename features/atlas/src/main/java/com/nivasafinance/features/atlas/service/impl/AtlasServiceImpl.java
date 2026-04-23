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
import com.nivasafinance.features.call.enums.TranscriptAiTool;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.service.CallWriteService;
import com.nivasafinance.features.dataprovider.service.DataProviderExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AtlasServiceImpl implements AtlasService {

    /**
     * Data provider {@value #ATLAS_DATA_PROVIDER_NAME} must return per-row:
     * {@code call_log_identifier}, {@code event_type}, {@code recording_url}.
     * Rows with any required field null/blank are skipped. No rows returned = nothing to enqueue.
     */
    private static final String ATLAS_DATA_PROVIDER_NAME = "atlas_data_provider";

    private static final String LEAD_IDENTIFIER_PARAM = "leadIdentifier";

    private static final String CALL_LOG_IDENTIFIER_COLUMN = "call_log_identifier";
    private static final String EVENT_TYPE_COLUMN = "event_type";
    private static final String RECORDING_URL_COLUMN = "recording_url";

    private static final String LEAD_IDENTIFIER = "leadIdentifier";
    private static final String CALL_LOG_IDENTIFIER = "callLogIdentifier";
    private static final String RECORDING_URL = "recordingUrl";
    private static final String EVENT_TYPE = "eventType";
    private static final String START_TIME = "startTime";

    private static final DateTimeFormatter START_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final MessagePublisherFactory messagePublisherFactory;
    private final MessagingProperties messagingProperties;
    private final CallReadService callReadService;
    private final CallWriteService callWriteService;
    private final DataProviderExecutor dataProviderExecutor;

    public AtlasServiceImpl(
            MessagePublisherFactory messagePublisherFactory,
            MessagingProperties messagingProperties,
            CallReadService callReadService,
            CallWriteService callWriteService,
            DataProviderExecutor dataProviderExecutor) {
        this.messagePublisherFactory = messagePublisherFactory;
        this.messagingProperties = messagingProperties;
        this.callReadService = callReadService;
        this.callWriteService = callWriteService;
        this.dataProviderExecutor = dataProviderExecutor;
    }

    @Override
    public void handleLeadCallLogCreated(LeadCallLogCreationEventPayload payload) {
        processAtlasForCallLog(payload.getLeadIdentifier(), payload.getCallLogIdentifier(), null);
    }

    @Override
    public void handleLeadCallLogUpdated(LeadCallLogUpdateEventPayload payload) {
        String recordingOverride = StringUtils.hasText(payload.getRecordingUrl()) ? payload.getRecordingUrl().trim() : null;
        processAtlasForCallLog(payload.getLeadIdentifier(), payload.getCallLogIdentifier(), recordingOverride);
    }

    @Override
    public void handleLeadStageTransitioned(StageTransitionEventPayload payload) {
        if (payload.getEntityType() != EntityType.LEAD) {
            return;
        }
        UUID leadIdentifier = payload.getEntityIdentifier();
        if (leadIdentifier == null) {
            log.debug("Skipping Atlas on stage transition — missing lead identifier");
            return;
        }
        if (!isNavigatorAtlasQueueConfigured()) {
            log.warn("Navigator Atlas queue is not configured; skipping transcription job");
            return;
        }
        List<Map<String, Object>> rows = loadAtlasDataProviderRows(leadIdentifier);
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (Map<String, Object> row : rows) {
            enqueueAtlasFromRow(leadIdentifier, row, null);
        }
    }

    private void processAtlasForCallLog(UUID leadIdentifier, UUID callLogIdentifier, String recordingUrlOverride) {
        if (!isNavigatorAtlasQueueConfigured()) {
            log.warn("Navigator Atlas queue is not configured; skipping transcription job");
            return;
        }
        if (leadIdentifier == null || callLogIdentifier == null) {
            log.warn("Lead call log event missing leadIdentifier; skipping Atlas job for callLog {}", callLogIdentifier);
            return;
        }
        List<Map<String, Object>> rows = loadAtlasDataProviderRows(leadIdentifier);
        if (rows == null || rows.isEmpty()) {
            return;
        }
        String target = callLogIdentifier.toString();
        for (Map<String, Object> row : rows) {
            if (target.equals(getStringValue(row, CALL_LOG_IDENTIFIER_COLUMN))) {
                enqueueAtlasFromRow(leadIdentifier, row, recordingUrlOverride);
                return;
            }
        }
    }

    private void enqueueAtlasFromRow(UUID leadIdentifier, Map<String, Object> row, String recordingUrlOverride) {
        String callLogIdentifierStr = getStringValue(row, CALL_LOG_IDENTIFIER_COLUMN);
        String eventType = getStringValue(row, EVENT_TYPE_COLUMN);
        String recordingUrl = StringUtils.hasText(recordingUrlOverride)
                ? recordingUrlOverride
                : getStringValue(row, RECORDING_URL_COLUMN);

        if (!StringUtils.hasText(callLogIdentifierStr) || !StringUtils.hasText(eventType) || !StringUtils.hasText(recordingUrl)) {
            log.debug("Skipping Atlas enqueue — missing required field(s) from data provider for lead {}", leadIdentifier);
            return;
        }

        UUID callLogIdentifier = UUID.fromString(callLogIdentifierStr);
        CallLogResponse callLog = callReadService.getCallLogByIdentifier(callLogIdentifier);
        if (shouldSkipForBolnaAnalysis(callLog.getAiAnalysis())) {
            log.debug("Skipping Atlas enqueue for callLog {} — analysis already present",
                    callLogIdentifier);
            return;
        }

        callWriteService.mergeAiAnalysisByIdentifier(
                callLogIdentifier,
                CallLog.AiAnalysisDetails.builder()
                        .status(AtlasJobStatus.INITIATED)
                        .transcriptAiTool(TranscriptAiTool.ATLAS)
                        .build());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(EVENT_TYPE, eventType.trim());
        body.put(LEAD_IDENTIFIER, leadIdentifier.toString());
        body.put(CALL_LOG_IDENTIFIER, callLogIdentifierStr);
        body.put(RECORDING_URL, recordingUrl);
        if (callLog.getCompletionDetails() != null && callLog.getCompletionDetails().getStartTime() != null) {
            body.put(START_TIME, callLog.getCompletionDetails().getStartTime().format(START_TIME_FORMATTER));
        }

        try {
            log.info("Pushing Atlas transcription job for callLog {}, lead {}", callLogIdentifierStr, leadIdentifier);
            messagePublisherFactory.getPublisher().publish(QueueType.NAVIGATOR_ATLAS, callLogIdentifierStr, body);
            log.info("Successfully pushed Atlas transcription job for callLog {}", callLogIdentifierStr);
        } catch (RuntimeException ex) {
            log.error("Failed to push Atlas transcription job for callLog {}, lead {}", callLogIdentifierStr, leadIdentifier, ex);
            callWriteService.mergeAiAnalysisByIdentifier(
                    callLogIdentifier,
                    CallLog.AiAnalysisDetails.builder()
                            .status(AtlasJobStatus.PUBLISHING_FAILED)
                            .transcriptAiTool(TranscriptAiTool.ATLAS)
                            .build());
        }
    }

    private List<Map<String, Object>> loadAtlasDataProviderRows(UUID leadIdentifier) {
        try {
            return dataProviderExecutor.executeDataProviderForList(
                    ATLAS_DATA_PROVIDER_NAME,
                    Map.of(LEAD_IDENTIFIER_PARAM, leadIdentifier));
        } catch (IllegalArgumentException ex) {
            log.warn("Atlas data provider '{}' missing or invalid; skipping", ATLAS_DATA_PROVIDER_NAME, ex);
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

    private static String getStringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString().trim() : null;
    }

    private static boolean shouldSkipForBolnaAnalysis(CallLog.AiAnalysisDetails ai) {
        if (ai == null) {
            return false;
        }
        return ai.getTranscriptAiTool() == TranscriptAiTool.BOLNA;
    }
}
