package com.nivasafinance.externals.atlas.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.atlas.dto.AtlasJobResponseDTO;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.AtlasJobStatus;
import com.nivasafinance.features.call.service.CallWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiConstants.ATLAS_V1)
@RequiredArgsConstructor
public class AtlasResultsController {

    private final CallWriteService callWriteService;
    private final MessageSource messageSource;

    @PostMapping("/results")
    public ResponseEntity<Void> receiveResults(@RequestBody AtlasJobResponseDTO body) {
        if (body.getCallLogIdentifier() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    messageSource.getMessage("error.atlas.webhook.call_log.required", null, LocaleContextHolder.getLocale()));
        }
        callWriteService.mergeAiAnalysisByIdentifier(body.getCallLogIdentifier(), toAiAnalysis(body));
        return ResponseEntity.noContent().build();
    }

    private static CallLog.AiAnalysisDetails toAiAnalysis(AtlasJobResponseDTO body) {
        return CallLog.AiAnalysisDetails.builder()
                .jobId(body.getJobId())
                .status(AtlasJobStatus.fromWireOrLegacy(body.getStatus()))
                .summaryUrl(body.getSummaryUrl())
                .analysisUrl(body.getAnalysisUrl())
                .transcriptUrl(body.getTranscriptUrl())
                .error(body.getError())
                .build();
    }
}
