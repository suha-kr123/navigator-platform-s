package com.nivasafinance.externals.atlas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nivasafinance.features.call.dto.CallAiAnalysisWebhookRequest;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.TranscriptAiTool;
import com.nivasafinance.features.call.service.CallWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CallAiAnalysisExternalControllerTest {

    @Mock
    private CallWriteService callWriteService;

    @InjectMocks
    private CallAiAnalysisExternalController controller;

    @Test
    void receiveResults_withValidBody_updatesAiAnalysisByProviderId() {
        ObjectNode extractedData = new ObjectMapper().createObjectNode().put("loanType", "Home loan");
        CallAiAnalysisWebhookRequest body = CallAiAnalysisWebhookRequest.builder()
                .id("execution-1")
                .agentId("agent-1")
                .summary("Conversation summary")
                .extractedData(extractedData)
                .telephonyData(CallAiAnalysisWebhookRequest.TelephonyData.builder()
                        .providerCallId("provider-call-id-1")
                        .build())
                .build();

        ResponseEntity<Void> result = controller.receiveResults(body);

        ArgumentCaptor<CallLog.AiAnalysisDetails> captor = ArgumentCaptor.forClass(CallLog.AiAnalysisDetails.class);
        verify(callWriteService).mergeAiAnalysisByProviderId(eq("provider-call-id-1"), captor.capture());

        CallLog.AiAnalysisDetails details = captor.getValue();
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode(),
                "Successful webhook handling should return HTTP 204 No Content");
        assertEquals("Conversation summary", details.getSummary(),
                "Webhook summary should be copied into aiAnalysis.summary");
        assertSame(extractedData, details.getExtractedData(),
                "Webhook extracted_data should be passed through into aiAnalysis.extractedData");
        assertEquals("execution-1", details.getExecutionId(),
                "Webhook id should be copied into aiAnalysis.executionId");
        assertEquals("agent-1", details.getAgentId(),
                "Webhook agent_id should be copied into aiAnalysis.agentId");
        assertEquals(TranscriptAiTool.BOLNA, details.getTranscriptAiTool(),
                "Bolna webhook should always mark transcriptAiTool as BOLNA");
    }
}
