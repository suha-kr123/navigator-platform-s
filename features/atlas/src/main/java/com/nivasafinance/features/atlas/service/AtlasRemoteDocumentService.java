package com.nivasafinance.features.atlas.service;

import com.nivasafinance.features.atlas.client.AtlasDocumentHttpClient;
import feign.FeignException;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Fetches AI analysis artifacts from the external Atlas service (Feign), using the same key layout as Atlas S3 paths.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AtlasRemoteDocumentService {

    private final AtlasDocumentHttpClient atlasDocumentHttpClient;

    public byte[] fetchTranscript(UUID callLogIdentifier) {
        String key = "transcripts/" + callLogIdentifier + ".txt";
        return executeAndReadBody(() -> atlasDocumentHttpClient.getTranscriptDocument(key), "transcript");
    }

    public byte[] fetchSummary(UUID callLogIdentifier) {
        String key = "summaries/" + callLogIdentifier + ".txt";
        return executeAndReadBody(() -> atlasDocumentHttpClient.getSummaryDocument(key), "summary");
    }

    public byte[] fetchAnalysis(UUID callLogIdentifier) {
        String key = "analyses/" + callLogIdentifier + "-raw.json";
        return executeAndReadBody(() -> atlasDocumentHttpClient.getAnalysisDocument(key), "analysis");
    }

    private byte[] executeAndReadBody(Supplier<Response> call, String artifactLabel) {
        try (Response response = call.get()) {
            return readBody(response, artifactLabel);
        } catch (FeignException e) {
            throw mapFeignException(e, artifactLabel);
        } catch (IOException e) {
            log.error("Atlas {} fetch failed: I/O error", artifactLabel, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
        }
    }

    private byte[] readBody(Response response, String artifactLabel) throws IOException {
        int status = response.status();
        if (status == HttpStatus.NOT_FOUND.value()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (status < 200 || status >= 300) {
            log.warn("Atlas {} returned HTTP {}", artifactLabel, status);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        }
        if (response.body() == null) {
            return new byte[0];
        }
        try (InputStream in = response.body().asInputStream()) {
            return in.readAllBytes();
        }
    }

    private RuntimeException mapFeignException(FeignException e, String artifactLabel) {
        int status = e.status();
        if (status == HttpStatus.NOT_FOUND.value()) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        log.error("Atlas {} fetch failed: Feign status {}", artifactLabel, status, e);
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
    }
}
