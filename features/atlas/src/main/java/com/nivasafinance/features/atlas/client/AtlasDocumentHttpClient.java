package com.nivasafinance.features.atlas.client;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Outbound client for Atlas service document endpoints (separate Atlas deployment).
 * Paths match Atlas: {@code GET /atlas/v1/...?key=...}.
 */
@FeignClient(name = "atlasDocuments", url = "${atlas.client.base-url}")
public interface AtlasDocumentHttpClient {

    @GetMapping("/atlas/v1/transcript_document")
    Response getTranscriptDocument(@RequestParam("key") String key);

    @GetMapping("/atlas/v1/summary_document")
    Response getSummaryDocument(@RequestParam("key") String key);

    @GetMapping("/atlas/v1/analysis_document")
    Response getAnalysisDocument(@RequestParam("key") String key);
}
