package com.nivasafinance.redash.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.redash.client.RedashClient;
import com.nivasafinance.redash.dto.FileType;
import com.nivasafinance.redash.dto.RedashQueryResponse;
import com.nivasafinance.redash.dto.RedashQueryResultRequest;
import com.nivasafinance.redash.dto.RedashReportRequest;
import feign.FeignException;
import feign.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

@Service
@AllArgsConstructor
@Slf4j
public class RedashService {

    private final RedashClient redashClient;
    private final ObjectMapper objectMapper;
    private static final int POLLING_INTERVAL_SECONDS = 2;
    private static final int MAX_TIMEOUT_SECONDS = 30;
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    public CompletableFuture<Response> generateReport(RedashReportRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Step 1: Build RedashQueryResultRequest from RedashReportRequest
                RedashQueryResultRequest queryRequest = RedashQueryResultRequest.builder()
                        .queryId(request.getQueryId())
                        .parameters(request.getParameters())
                        .maxAge(0L)
                        .build();

                // Step 2: Execute query to start the job
                log.info("Executing Redash query with ID: {}", request.getQueryId());
                RedashQueryResponse queryResponse = redashClient.executeQuery(request.getQueryId(), queryRequest);
                
                if (queryResponse == null || queryResponse.getJob() == null || queryResponse.getJob().getId() == null) {
                    throw new RuntimeException("Failed to start query execution: Invalid response from Redash");
                }

                String jobId = queryResponse.getJob().getId();
                log.info("Query execution started with job ID: {}", jobId);

                // Step 3: Poll for job completion
                long startTime = System.currentTimeMillis();
                RedashQueryResponse.Job job;
                
                do {
                    // Check timeout
                    long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                    if (elapsedTime >= MAX_TIMEOUT_SECONDS) {
                        throw new TimeoutException("Query execution timed out after " + MAX_TIMEOUT_SECONDS + " seconds");
                    }

                    // Poll job status
                    Thread.sleep(POLLING_INTERVAL_SECONDS * 1000);
                    log.debug("Polling job status for job ID: {}", jobId);
                    RedashQueryResponse jobStatusResponse = redashClient.getJobStatus(jobId);
                    
                    if (jobStatusResponse == null || jobStatusResponse.getJob() == null) {
                        throw new RuntimeException("Failed to get job status: Invalid response from Redash");
                    }

                    job = jobStatusResponse.getJob();
                    Long status = job.getStatus();

                    // Check job status
                    if (status == 4) { // FAILURE
                        String errorMessage = job.getError() != null ? job.getError() : "Query execution failed";
                        throw new RuntimeException("Query execution failed: " + errorMessage);
                    } else if (status == 5) { // CANCELLED
                        throw new RuntimeException("Query execution was cancelled");
                    } else if (status == 3) { // SUCCESS
                        if (job.getQueryResultId() == null || job.getQueryResultId().isEmpty()) {
                            throw new RuntimeException("Query execution succeeded but query_result_id is missing");
                        }
                        log.info("Query execution completed successfully with result ID: {}", job.getQueryResultId());
                        break;
                    }
                    // Status 1 (PENDING) or 2 (STARTED) - continue polling
                    log.debug("Job status: {} (PENDING=1, STARTED=2, SUCCESS=3, FAILURE=4, CANCELLED=5)", status);
                } while (true);

                // Step 4: Download the file
                String queryResultId = job.getQueryResultId();
                String fileExtension = getFileExtension(request.getFileType());
                log.info("Downloading query result file: {}.{}", queryResultId, fileExtension);
                
                Response fileResponse = redashClient.downloadQueryResult(queryResultId, fileExtension);
                
                if (fileResponse == null) {
                    throw new RuntimeException("Failed to download query result file");
                }

                log.info("Successfully downloaded query result file");
                return fileResponse;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Query execution was interrupted", e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (FeignException e) {
                String errorMessage = extractErrorFromFeignException(e);
                log.error("Feign error generating Redash report: {}", errorMessage, e);
                throw new RuntimeException(errorMessage);
            } catch (Exception e) {
                log.error("Error generating Redash report", e);
                throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
            }
        }, executorService);
    }

    private String getFileExtension(FileType fileType) {
        if (fileType == null) {
            throw new IllegalArgumentException("FileType cannot be null");
        }
        return fileType.name().toLowerCase();
    }

    private String extractErrorFromFeignException(FeignException e) {
        try {
            String responseBody = e.contentUTF8();
            if (responseBody != null && !responseBody.isEmpty()) {
                // Try parsing as object first
                try {
                    RedashQueryResponse response = objectMapper.readValue(responseBody, RedashQueryResponse.class);
                    if (response != null && response.getJob() != null && response.getJob().getError() != null) {
                        return response.getJob().getError();
                    }
                } catch (Exception objectParseException) {
                    // If object parsing fails, try parsing as array
                    try {
                        RedashQueryResponse[] responses = objectMapper.readValue(responseBody, RedashQueryResponse[].class);
                        if (responses != null && responses.length > 0 && responses[0].getJob() != null 
                                && responses[0].getJob().getError() != null) {
                            return responses[0].getJob().getError();
                        }
                    } catch (Exception arrayParseException) {
                        log.debug("Failed to parse response as both object and array", arrayParseException);
                    }
                }
            }
        } catch (Exception parseException) {
            log.warn("Failed to parse Feign error response, using default error message", parseException);
        }
        // Fallback to a generic error message if parsing fails
        return "Failed to execute Redash query";
    }
}
