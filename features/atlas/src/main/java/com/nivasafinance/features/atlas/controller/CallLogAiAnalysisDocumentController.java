package com.nivasafinance.features.atlas.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.atlas.service.AtlasRemoteDocumentService;
import com.nivasafinance.features.call.service.CallReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Proxies AI analysis artifacts from the external Atlas HTTP API (same contract as Atlas {@code /atlas/v1/...}).
 */
@RestController
@RequestMapping(ApiConstants.V1 + "/calls/{callLogIdentifier}/ai-analysis")
@RequiredArgsConstructor
public class CallLogAiAnalysisDocumentController {

    private final CallReadService callReadService;
    private final AtlasRemoteDocumentService atlasRemoteDocumentService;

    @GetMapping("/transcript")
    @RequirePermission(permissionName = "READ_LEAD_CALL")
    public ResponseEntity<byte[]> getTranscript(@PathVariable UUID callLogIdentifier) {
        callReadService.getCallLogByIdentifier(callLogIdentifier);
        byte[] body = atlasRemoteDocumentService.fetchTranscript(callLogIdentifier);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"transcript.txt\"")
                .body(body);
    }

    @GetMapping("/summary")
    @RequirePermission(permissionName = "READ_LEAD_CALL")
    public ResponseEntity<byte[]> getSummary(@PathVariable UUID callLogIdentifier) {
        callReadService.getCallLogByIdentifier(callLogIdentifier);
        byte[] body = atlasRemoteDocumentService.fetchSummary(callLogIdentifier);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"summary.txt\"")
                .body(body);
    }

    @GetMapping("/analysis")
    @RequirePermission(permissionName = "READ_LEAD_CALL")
    public ResponseEntity<byte[]> getAnalysis(@PathVariable UUID callLogIdentifier) {
        callReadService.getCallLogByIdentifier(callLogIdentifier);
        byte[] body = atlasRemoteDocumentService.fetchAnalysis(callLogIdentifier);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"analysis.json\"")
                .body(body);
    }
}
