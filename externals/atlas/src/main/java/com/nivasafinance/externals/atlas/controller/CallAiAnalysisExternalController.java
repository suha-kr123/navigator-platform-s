package com.nivasafinance.externals.atlas.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.call.dto.CallAiAnalysisWebhookRequest;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.TranscriptAiTool;
import com.nivasafinance.features.call.service.CallWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.OPEN_API_V1 + "/calls/ai-analysis")
@RequiredArgsConstructor
public class CallAiAnalysisExternalController {

    private final CallWriteService callWriteService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveResults(@RequestBody CallAiAnalysisWebhookRequest body) {
        callWriteService.mergeAiAnalysisByProviderId(providerCallId(body), toAiAnalysis(body));
        return ResponseEntity.noContent().build();
    }

    private static String providerCallId(CallAiAnalysisWebhookRequest body) {
        return body.getTelephonyData() != null ? body.getTelephonyData().getProviderCallId() : null;
    }

    private static CallLog.AiAnalysisDetails toAiAnalysis(CallAiAnalysisWebhookRequest body) {
        return CallLog.AiAnalysisDetails.builder()
                .summary(body.getSummary())
                .extractedData(body.getExtractedData())
                .executionId(body.getId())
                .agentId(body.getAgentId())
                .transcriptAiTool(TranscriptAiTool.BOLNA)
                .build();
    }
}
