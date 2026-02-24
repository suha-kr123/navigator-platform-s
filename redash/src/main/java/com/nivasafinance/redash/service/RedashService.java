package com.nivasafinance.redash.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.redash.client.RedashClient;
import com.nivasafinance.redash.dto.ExcelMergeInput;
import com.nivasafinance.redash.dto.FileType;
import com.nivasafinance.redash.dto.RedashExcelReportRequest;
import com.nivasafinance.redash.dto.RedashQueryResponse;
import com.nivasafinance.redash.dto.RedashQueryResultRequest;
import com.nivasafinance.redash.dto.RedashReportRequest;
import feign.FeignException;
import feign.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

@Service
@AllArgsConstructor
@Slf4j
public class RedashService {

    private static final String SHEET_NAME_PREFIX = "Sheet";

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

    /**
     * Generates a consolidated Excel report by running each Redash query (by the given query IDs)
     * with the given parameters (XLSX), merging all sheets into one workbook, and returning the Excel as a stream.
     * Caller supplies the list of query IDs; Redash does not store or resolve query IDs.
     *
     * @param request RedashExcelReportRequest containing queryIds, parameters, and optional queryIdToSheetName mapping
     * @return InputStream of the merged Excel workbook; caller is responsible for closing it
     */
    public InputStream generateExcelReport(RedashExcelReportRequest request) {
        if (request == null || request.getQueryIds() == null || request.getQueryIds().isEmpty()) {
            throw new IllegalArgumentException("request and queryIds must not be null or empty");
        }

        List<Long> queryIds = request.getQueryIds();
        Map<String, Object> params = request.getParameters() != null ? request.getParameters() : Map.of();
        Map<Long, String> queryIdToSheetName = request.getQueryIdToSheetName();

        List<CompletableFuture<Response>> futures = queryIds.stream()
                .map(queryId -> {
                    RedashReportRequest reportRequest = RedashReportRequest.builder()
                            .queryId(queryId)
                            .fileType(FileType.XLSX)
                            .parameters(params)
                            .build();
                    return generateReport(reportRequest);
                })
                .toList();

        byte[] mergedBytes = CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> {
                    List<Response> responses = futures.stream()
                            .map(CompletableFuture::join)
                            .toList();
                    ExcelMergeInput mergeInput = ExcelMergeInput.builder()
                            .queryIds(queryIds)
                            .responses(responses)
                            .queryIdToSheetName(queryIdToSheetName)
                            .build();
                    return mergeExcelSheets(mergeInput);
                })
                .join();

        return new ByteArrayInputStream(mergedBytes);
    }

    /**
     * Merges XLSX responses from Redash (one sheet per response) into a single workbook.
     * Each response body is read as XLSX; the first sheet is copied into the target workbook.
     * Sheet names are taken from queryIdToSheetName mapping if available, otherwise defaults to Sheet1, Sheet2, etc.
     *
     * @param input merge input containing queryIds, responses (same order), and optional queryIdToSheetName mapping
     */
    private byte[] mergeExcelSheets(ExcelMergeInput input) {
        List<Long> queryIds = input.getQueryIds();
        List<Response> responses = input.getResponses();
        Map<Long, String> queryIdToSheetName = input.getQueryIdToSheetName();

        try (Workbook targetWorkbook = new XSSFWorkbook()) {
            for (int i = 0; i < responses.size(); i++) {
                Response response = responses.get(i);
                Long queryId = i < queryIds.size() ? queryIds.get(i) : null;

                String sheetName;
                if (queryIdToSheetName != null && queryId != null && queryIdToSheetName.containsKey(queryId)) {
                    sheetName = queryIdToSheetName.get(queryId);
                } else {
                    sheetName = SHEET_NAME_PREFIX + (i + 1);
                }

                if (response == null || response.body() == null) {
                    log.warn("Skipping sheet '{}': null response body", sheetName);
                    continue;
                }
                try (InputStream bodyStream = response.body().asInputStream();
                     Workbook sourceWorkbook = new XSSFWorkbook(bodyStream)) {
                    Sheet sourceSheet = sourceWorkbook.getSheetAt(0);
                    Sheet targetSheet = targetWorkbook.createSheet(sanitizeSheetName(sheetName));
                    copySheet(sourceSheet, targetSheet);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            targetWorkbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to merge Excel sheets", e);
            throw new RuntimeException("Failed to merge Excel report: " + e.getMessage(), e);
        }
    }

    private void copySheet(Sheet source, Sheet target) {
        for (Row sourceRow : source) {
            Row targetRow = target.createRow(sourceRow.getRowNum());
            for (Cell sourceCell : sourceRow) {
                Cell targetCell = targetRow.createCell(sourceCell.getColumnIndex(), sourceCell.getCellType());
                copyCellValue(sourceCell, targetCell);
            }
        }
    }

    private void copyCellValue(Cell source, Cell target) {
        CellType cellType = source.getCellType();
        switch (cellType) {
            case STRING -> target.setCellValue(source.getStringCellValue());
            case NUMERIC -> target.setCellValue(source.getNumericCellValue());
            case BOOLEAN -> target.setCellValue(source.getBooleanCellValue());
            case FORMULA -> target.setCellFormula(source.getCellFormula());
            case BLANK -> target.setBlank();
            case ERROR -> target.setCellErrorValue(source.getErrorCellValue());
            default -> { }
        }
    }

    private String sanitizeSheetName(String name) {
        if (name == null || name.isEmpty()) {
            return "Sheet";
        }
        String sanitized = name.replaceAll("[\\\\/:*?\\[\\]]", "_");
        return sanitized.substring(0, Math.min(sanitized.length(), 31));
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

    /**
     * Executes a Redash query (with poll-and-download pattern), downloads result as JSON,
     * and returns the first row as a map. Caller is responsible for looping over multiple query IDs.
     *
     * @param queryId     Redash query ID
     * @param parameters  optional query parameters (can be null)
     * @return first row from query_result.data.rows, or null if no rows
     */
    public Map<String, Object> getQueryResult(Long queryId, Map<String, Object> parameters) {
        try {
            RedashQueryResultRequest queryRequest = RedashQueryResultRequest.builder()
                    .queryId(queryId)
                    .parameters(parameters != null ? parameters : Map.of())
                    .maxAge(0L)
                    .build();

            log.info("Executing Redash query with ID: {}", queryId);
            RedashQueryResponse queryResponse = redashClient.executeQuery(queryId, queryRequest);
            if (queryResponse == null || queryResponse.getJob() == null || queryResponse.getJob().getId() == null) {
                throw new RuntimeException("Failed to start query execution: Invalid response from Redash");
            }

            String jobId = queryResponse.getJob().getId();
            String queryResultId = pollForQueryResultId(jobId);

            String json = redashClient.downloadQueryResultAsJson(queryResultId);
            if (json == null || json.isBlank()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode rows = root.path("query_result").path("data").path("rows");
            if (!rows.isArray() || rows.isEmpty()) {
                return null;
            }
            JsonNode firstRow = rows.get(0);
            return objectMapper.convertValue(firstRow, new TypeReference<Map<String, Object>>() { });
        } catch (FeignException e) {
            String errorMessage = extractErrorFromFeignException(e);
            log.error("Feign error in getQueryResult: {}", errorMessage, e);
            throw new RuntimeException(errorMessage);
        } catch (Exception e) {
            log.error("Error in getQueryResult for queryId {}", queryId, e);
            throw new RuntimeException("Failed to get query result: " + e.getMessage(), e);
        }
    }

    private String pollForQueryResultId(String jobId) throws InterruptedException, TimeoutException {
        long startTime = System.currentTimeMillis();
        RedashQueryResponse.Job job;
        do {
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            if (elapsedTime >= MAX_TIMEOUT_SECONDS) {
                throw new TimeoutException("Query execution timed out after " + MAX_TIMEOUT_SECONDS + " seconds");
            }
            Thread.sleep(POLLING_INTERVAL_SECONDS * 1000L);
            log.debug("Polling job status for job ID: {}", jobId);
            RedashQueryResponse jobStatusResponse = redashClient.getJobStatus(jobId);
            if (jobStatusResponse == null || jobStatusResponse.getJob() == null) {
                throw new RuntimeException("Failed to get job status: Invalid response from Redash");
            }
            job = jobStatusResponse.getJob();
            Long status = job.getStatus();
            if (status == 4) {
                String errorMessage = job.getError() != null ? job.getError() : "Query execution failed";
                throw new RuntimeException("Query execution failed: " + errorMessage);
            }
            if (status == 5) {
                throw new RuntimeException("Query execution was cancelled");
            }
            if (status == 3) {
                if (job.getQueryResultId() == null || job.getQueryResultId().isEmpty()) {
                    throw new RuntimeException("Query execution succeeded but query_result_id is missing");
                }
                log.info("Query execution completed with result ID: {}", job.getQueryResultId());
                return job.getQueryResultId();
            }
        } while (true);
    }
}
