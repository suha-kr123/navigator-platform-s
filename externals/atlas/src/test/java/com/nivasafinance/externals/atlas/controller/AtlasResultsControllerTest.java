package com.nivasafinance.externals.atlas.controller;

import com.nivasafinance.features.atlas.dto.AtlasJobResponseDTO;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.AtlasJobStatus;
import com.nivasafinance.features.call.enums.TranscriptAiTool;
import com.nivasafinance.features.call.service.CallWriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtlasResultsControllerTest {

    @Mock
    private CallWriteService callWriteService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AtlasResultsController controller;

    private UUID callLogIdentifier;

    @BeforeEach
    void setUp() {
        callLogIdentifier = UUID.randomUUID();
    }

    // ── receiveResults: validation ──

    @Test
    void receiveResults_withNullCallLogIdentifier_throwsBadRequest() {
        AtlasJobResponseDTO body = AtlasJobResponseDTO.builder()
                .callLogIdentifier(null)
                .jobId("job-1")
                .status("completed")
                .build();
        when(messageSource.getMessage(eq("error.atlas.webhook.call_log.required"), any(), any()))
                .thenReturn("Call log identifier is required");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.receiveResults(body),
                "Null callLogIdentifier should throw ResponseStatusException");
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode(),
                "Exception status should be 400 BAD_REQUEST");
    }

    // ── receiveResults: happy path ──

    @Test
    void receiveResults_withValidBody_returnsNoContent() {
        AtlasJobResponseDTO body = AtlasJobResponseDTO.builder()
                .callLogIdentifier(callLogIdentifier)
                .jobId("job-1")
                .status("completed")
                .summaryUrl("https://example.com/summary")
                .analysisUrl("https://example.com/analysis")
                .transcriptUrl("https://example.com/transcript")
                .build();

        ResponseEntity<Void> result = controller.receiveResults(body);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode(),
                "Successful receiveResults should return HTTP 204 No Content");
        verify(callWriteService).mergeAiAnalysisByIdentifier(eq(callLogIdentifier), any(CallLog.AiAnalysisDetails.class));
    }

    @Test
    void receiveResults_withValidBody_passesCorrectAiAnalysisDetails() {
        AtlasJobResponseDTO body = AtlasJobResponseDTO.builder()
                .callLogIdentifier(callLogIdentifier)
                .jobId("job-1")
                .status("completed")
                .summaryUrl("https://example.com/summary")
                .analysisUrl("https://example.com/analysis")
                .transcriptUrl("https://example.com/transcript")
                .error(null)
                .build();

        controller.receiveResults(body);

        ArgumentCaptor<CallLog.AiAnalysisDetails> captor = ArgumentCaptor.forClass(CallLog.AiAnalysisDetails.class);
        verify(callWriteService).mergeAiAnalysisByIdentifier(eq(callLogIdentifier), captor.capture());

        CallLog.AiAnalysisDetails details = captor.getValue();
        assertEquals("job-1", details.getJobId(), "AiAnalysisDetails jobId should match request body");
        assertEquals(AtlasJobStatus.COMPLETED, details.getStatus(), "AiAnalysisDetails status should be mapped to COMPLETED");
        assertEquals("https://example.com/summary", details.getSummaryUrl(), "AiAnalysisDetails summaryUrl should match request body");
        assertEquals("https://example.com/analysis", details.getAnalysisUrl(), "AiAnalysisDetails analysisUrl should match request body");
        assertEquals("https://example.com/transcript", details.getTranscriptUrl(), "AiAnalysisDetails transcriptUrl should match request body");
        assertEquals(TranscriptAiTool.ATLAS, details.getTranscriptAiTool(),
                "Atlas webhook should always mark transcriptAiTool as ATLAS");
        assertNull(details.getError(), "AiAnalysisDetails error should be null when not provided");
    }

    @Test
    void receiveResults_withFailedStatus_mapsStatusCorrectly() {
        AtlasJobResponseDTO body = AtlasJobResponseDTO.builder()
                .callLogIdentifier(callLogIdentifier)
                .jobId("job-2")
                .status("failed")
                .error("transcription timeout")
                .build();

        controller.receiveResults(body);

        ArgumentCaptor<CallLog.AiAnalysisDetails> captor = ArgumentCaptor.forClass(CallLog.AiAnalysisDetails.class);
        verify(callWriteService).mergeAiAnalysisByIdentifier(eq(callLogIdentifier), captor.capture());

        assertEquals(AtlasJobStatus.FAILED, captor.getValue().getStatus(),
                "AiAnalysisDetails status should be mapped to FAILED");
        assertEquals("transcription timeout", captor.getValue().getError(),
                "AiAnalysisDetails error should match request body error");
    }

    @Test
    void receiveResults_withNullStatus_mapsStatusToNull() {
        AtlasJobResponseDTO body = AtlasJobResponseDTO.builder()
                .callLogIdentifier(callLogIdentifier)
                .jobId("job-3")
                .status(null)
                .build();

        controller.receiveResults(body);

        ArgumentCaptor<CallLog.AiAnalysisDetails> captor = ArgumentCaptor.forClass(CallLog.AiAnalysisDetails.class);
        verify(callWriteService).mergeAiAnalysisByIdentifier(eq(callLogIdentifier), captor.capture());

        assertNull(captor.getValue().getStatus(),
                "AiAnalysisDetails status should be null when request status is null");
    }

    // ── receiveResults: interaction verification ──

    @Test
    void receiveResults_withNullCallLogIdentifier_doesNotCallService() {
        AtlasJobResponseDTO body = AtlasJobResponseDTO.builder()
                .callLogIdentifier(null)
                .build();
        when(messageSource.getMessage(eq("error.atlas.webhook.call_log.required"), any(), any()))
                .thenReturn("required");

        assertThrows(ResponseStatusException.class, () -> controller.receiveResults(body));

        verifyNoInteractions(callWriteService);
    }
}
