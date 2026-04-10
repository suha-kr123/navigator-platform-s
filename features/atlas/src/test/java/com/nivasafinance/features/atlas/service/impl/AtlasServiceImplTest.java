package com.nivasafinance.features.atlas.service.impl;

import com.nivasafinance.common.events.payload.LeadCallLogCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadCallLogUpdateEventPayload;
import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.MessageProvider;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.common.messaging.publisher.MessagePublisher;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.AtlasJobStatus;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.repository.CallLogLeadRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.service.CallWriteService;
import com.nivasafinance.features.dataprovider.service.DataProviderExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtlasServiceImplTest {

    @Mock
    private MessagePublisherFactory messagePublisherFactory;
    @Mock
    private MessagingProperties messagingProperties;
    @Mock
    private CallReadService callReadService;
    @Mock
    private CallWriteService callWriteService;
    @Mock
    private DataProviderExecutor dataProviderExecutor;
    @Mock
    private CallLogLeadRepositoryWrapper callLogLeadRepositoryWrapper;
    @Mock
    private MessagePublisher messagePublisher;

    @InjectMocks
    private AtlasServiceImpl service;

    @Test
    void handleLeadCallLogCreated_publishesWhenRecordingAvailable() {
        configureSqsQueue();
        UUID callLogId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        CallLogResponse callLog = callLogWithRecording("https://rec", AtlasJobStatus.COMPLETED);
        when(callReadService.getCallLogByIdentifier(callLogId)).thenReturn(callLog);
        when(messagePublisherFactory.getPublisher()).thenReturn(messagePublisher);
        stubAtlasLeadRowForLead(leadId, true, "CSE");

        service.handleLeadCallLogCreated(LeadCallLogCreationEventPayload.builder()
                .callLogIdentifier(callLogId)
                .leadIdentifier(leadId)
                .build());

        verify(callWriteService).mergeAiAnalysisByIdentifier(eq(callLogId),
                argThat(ai -> ai.getStatus() == AtlasJobStatus.INITIATED));
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagePublisher).publish(eq(QueueType.NAVIGATOR_ATLAS), eq(callLogId.toString()), payloadCaptor.capture());
        assertEquals(leadId.toString(), payloadCaptor.getValue().get("leadIdentifier"));
        assertEquals("CSE", payloadCaptor.getValue().get("eventType"));
    }

    @Test
    void handleLeadCallLogUpdated_publishFails_marksPublishingFailed() {
        configureSqsQueue();
        UUID callLogId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        CallLogResponse callLog = callLogWithRecording("https://rec", null);
        when(callReadService.getCallLogByIdentifier(callLogId)).thenReturn(callLog);
        when(messagePublisherFactory.getPublisher()).thenReturn(messagePublisher);
        doThrow(new RuntimeException("fail")).when(messagePublisher)
                .publish(eq(QueueType.NAVIGATOR_ATLAS), anyString(), anyMap());
        stubAtlasLeadRowForLead(leadId, true, "CSE");

        service.handleLeadCallLogUpdated(LeadCallLogUpdateEventPayload.builder()
                .callLogIdentifier(callLogId)
                .leadIdentifier(leadId)
                .recordingUrl("https://rec")
                .build());

        verify(callWriteService).mergeAiAnalysisByIdentifier(eq(callLogId),
                argThat(ai -> ai.getStatus() == AtlasJobStatus.INITIATED));
        verify(callWriteService).mergeAiAnalysisByIdentifier(eq(callLogId),
                argThat(ai -> ai.getStatus() == AtlasJobStatus.PUBLISHING_FAILED));
    }

    @Test
    void handleLeadCallLogUpdated_missingRecording_skips() {
        configureSqsQueue();
        UUID callLogId = UUID.randomUUID();
        CallLogResponse callLog = callLogWithRecording(null, null);
        when(callReadService.getCallLogByIdentifier(callLogId)).thenReturn(callLog);

        service.handleLeadCallLogUpdated(LeadCallLogUpdateEventPayload.builder()
                .callLogIdentifier(callLogId)
                .leadIdentifier(UUID.randomUUID())
                .recordingUrl("  ")
                .build());

        verify(callWriteService, never()).mergeAiAnalysisByIdentifier(any(), any());
        verify(messagePublisher, never()).publish(any(), any(), any());
    }

    @Test
    void handleLeadCallLogCreated_existingInitiatedJob_skips() {
        configureSqsQueue();
        CallLogResponse callLog = callLogWithRecording("https://rec", AtlasJobStatus.INITIATED);
        when(callReadService.getCallLogByIdentifier(any())).thenReturn(callLog);

        service.handleLeadCallLogCreated(LeadCallLogCreationEventPayload.builder()
                .callLogIdentifier(UUID.randomUUID())
                .leadIdentifier(UUID.randomUUID())
                .build());

        verify(callWriteService, never()).mergeAiAnalysisByIdentifier(any(), any());
        verify(messagePublisher, never()).publish(any(), any(), any());
    }

    @Test
    void handleLeadCallLogCreated_queueNotConfigured_skipsPublish() {
        when(messagingProperties.getProvider()).thenReturn(MessageProvider.SQS);
        MessagingProperties.SqsProperties sqs = new MessagingProperties.SqsProperties();
        when(messagingProperties.getSqs()).thenReturn(sqs); // no queues set -> not configured

        service.handleLeadCallLogCreated(LeadCallLogCreationEventPayload.builder()
                .callLogIdentifier(UUID.randomUUID())
                .leadIdentifier(UUID.randomUUID())
                .build());

        verify(callReadService, never()).getCallLogByIdentifier(any());
        verify(messagePublisher, never()).publish(any(), any(), any());
    }

    @Test
    void handleLeadCallLogCreated_dataProviderPrimaryRoleSme_skipsAtlasEnqueue() {
        configureSqsQueue();
        UUID callLogId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        CallLogResponse callLog = callLogWithRecording("https://rec", null);
        when(callReadService.getCallLogByIdentifier(callLogId)).thenReturn(callLog);
        stubAtlasLeadRowForLead(leadId, true, "SME");

        service.handleLeadCallLogCreated(LeadCallLogCreationEventPayload.builder()
                .callLogIdentifier(callLogId)
                .leadIdentifier(leadId)
                .build());

        verify(callWriteService, never()).mergeAiAnalysisByIdentifier(any(), any());
        verify(messagePublisher, never()).publish(any(), any(), any());
    }

    @Test
    void handleLeadCallLogCreated_expertScreeningBlankPrimaryRole_skipsAtlasEnqueue() {
        configureSqsQueue();
        UUID callLogId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        CallLogResponse callLog = callLogWithRecording("https://rec", null);
        when(callReadService.getCallLogByIdentifier(callLogId)).thenReturn(callLog);
        stubAtlasLeadRowForLead(leadId, true, "");

        service.handleLeadCallLogCreated(LeadCallLogCreationEventPayload.builder()
                .callLogIdentifier(callLogId)
                .leadIdentifier(leadId)
                .build());

        verify(callWriteService, never()).mergeAiAnalysisByIdentifier(any(), any());
        verify(messagePublisher, never()).publish(any(), any(), any());
    }

    @Test
    void handleLeadCallLogCreated_stageEligibleFalse_skipsAtlasEnqueue() {
        configureSqsQueue();
        UUID callLogId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        CallLogResponse callLog = callLogWithRecording("https://rec", null);
        when(callReadService.getCallLogByIdentifier(callLogId)).thenReturn(callLog);
        stubAtlasLeadRowForLead(leadId, false, "CSE");

        service.handleLeadCallLogCreated(LeadCallLogCreationEventPayload.builder()
                .callLogIdentifier(callLogId)
                .leadIdentifier(leadId)
                .build());

        verify(callWriteService, never()).mergeAiAnalysisByIdentifier(any(), any());
        verify(messagePublisher, never()).publish(any(), any(), any());
    }

    private void configureSqsQueue() {
        MessagingProperties.SqsProperties sqs = new MessagingProperties.SqsProperties();
        sqs.getQueues().put(QueueType.NAVIGATOR_ATLAS, "queue-url");
        when(messagingProperties.getProvider()).thenReturn(MessageProvider.SQS);
        when(messagingProperties.getSqs()).thenReturn(sqs);
        lenient().when(messagePublisherFactory.getPublisher()).thenReturn(messagePublisher);
    }

    private void stubAtlasLeadRowForLead(UUID leadId, boolean stageEligible, String primaryRole) {
        when(dataProviderExecutor.executeDataProvider(eq("atlas_lead_stage"), argThat(m -> leadId.equals(m.get("leadIdentifier")))))
                .thenReturn(Map.of("stage_eligible", Boolean.toString(stageEligible), "primary_role", primaryRole));
    }

    private CallLogResponse callLogWithRecording(String url, AtlasJobStatus aiStatus) {
        CallLogResponse callLog = new CallLogResponse();
        CallLog.RecordingDetails recordingDetails = CallLog.RecordingDetails.builder().url(url).build();
        callLog.setRecordingDetails(recordingDetails);
        callLog.setDirection(CallDirection.OUTBOUND);
        callLog.setFromNumber("12345");
        CallLog.AiAnalysisDetails ai = null;
        if (aiStatus != null) {
            ai = CallLog.AiAnalysisDetails.builder().status(aiStatus).jobId("job").build();
        }
        callLog.setAiAnalysis(ai);
        return callLog;
    }
}
