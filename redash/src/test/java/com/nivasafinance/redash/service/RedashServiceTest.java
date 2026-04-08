package com.nivasafinance.redash.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.redash.client.RedashClient;
import com.nivasafinance.redash.dto.FileType;
import com.nivasafinance.redash.dto.RedashExcelReportRequest;
import com.nivasafinance.redash.dto.RedashQueryResponse;
import com.nivasafinance.redash.dto.RedashReportRequest;
import feign.FeignException;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedashServiceTest {

    @Mock
    private RedashClient redashClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RedashService redashService;

    // ── getQueryResult: happy paths ──

    @Test
    void getQueryResult_whenQuerySucceedsWithRows_returnsFirstRow() {
        Long queryId = 1L;
        setupSuccessfulPolling(queryId, "job-1", "result-1");
        when(redashClient.downloadQueryResultAsJson("result-1"))
                .thenReturn("{\"query_result\":{\"data\":{\"rows\":[{\"col1\":\"val1\",\"col2\":2}]}}}");

        Map<String, Object> result = redashService.getQueryResult(queryId, Map.of("p", "v"));

        assertNotNull(result, "Should return the first row from query result");
        assertEquals("val1", result.get("col1"), "First row col1 should match the query result data");
    }

    @Test
    void getQueryResult_whenQuerySucceedsWithNoRows_returnsNull() {
        Long queryId = 2L;
        setupSuccessfulPolling(queryId, "job-2", "result-2");
        when(redashClient.downloadQueryResultAsJson("result-2"))
                .thenReturn("{\"query_result\":{\"data\":{\"rows\":[]}}}");

        Map<String, Object> result = redashService.getQueryResult(queryId, Map.of());

        assertNull(result, "Should return null when query result contains no rows");
    }

    @Test
    void getQueryResult_whenDownloadedJsonIsBlank_returnsNull() {
        Long queryId = 3L;
        setupSuccessfulPolling(queryId, "job-3", "result-3");
        when(redashClient.downloadQueryResultAsJson("result-3")).thenReturn("   ");

        Map<String, Object> result = redashService.getQueryResult(queryId, Map.of());

        assertNull(result, "Should return null when downloaded JSON is blank");
    }

    @Test
    void getQueryResult_withNullParameters_succeeds() {
        Long queryId = 4L;
        setupSuccessfulPolling(queryId, "job-4", "result-4");
        when(redashClient.downloadQueryResultAsJson("result-4"))
                .thenReturn("{\"query_result\":{\"data\":{\"rows\":[{\"id\":1}]}}}");

        Map<String, Object> result = redashService.getQueryResult(queryId, null);

        assertNotNull(result, "Should handle null parameters and return the first row");
    }

    // ── getQueryResult: executeQuery failures ──

    @Test
    void getQueryResult_whenExecuteQueryReturnsNull_throwsRuntimeException() {
        when(redashClient.executeQuery(eq(1L), any())).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redashService.getQueryResult(1L, Map.of()),
                "Should throw when executeQuery returns null");
        assertTrue(ex.getMessage().contains("Invalid response from Redash"),
                "Exception message should indicate invalid response");
    }

    @Test
    void getQueryResult_whenExecuteQueryReturnsNullJob_throwsRuntimeException() {
        RedashQueryResponse response = RedashQueryResponse.builder().job(null).build();
        when(redashClient.executeQuery(eq(1L), any())).thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redashService.getQueryResult(1L, Map.of()),
                "Should throw when executeQuery response has null job");
        assertTrue(ex.getMessage().contains("Invalid response from Redash"),
                "Exception message should indicate invalid response");
    }

    @Test
    void getQueryResult_whenExecuteQueryReturnsNullJobId_throwsRuntimeException() {
        RedashQueryResponse response = RedashQueryResponse.builder()
                .job(RedashQueryResponse.Job.builder().id(null).build())
                .build();
        when(redashClient.executeQuery(eq(1L), any())).thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redashService.getQueryResult(1L, Map.of()),
                "Should throw when executeQuery response has null job id");
        assertTrue(ex.getMessage().contains("Invalid response from Redash"),
                "Exception message should indicate invalid response");
    }

    // ── getQueryResult: polling failures ──

    @Test
    void getQueryResult_whenJobStatusIsFailed_throwsRuntimeException() {
        Long queryId = 1L;
        setupExecuteQueryResponse(queryId, "job-1");

        RedashQueryResponse failedResponse = RedashQueryResponse.builder()
                .job(RedashQueryResponse.Job.builder()
                        .id("job-1").status(4L).error("Syntax error in query")
                        .build())
                .build();
        when(redashClient.getJobStatus("job-1")).thenReturn(failedResponse);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redashService.getQueryResult(queryId, Map.of()),
                "Should throw when job status indicates failure");
        assertTrue(ex.getMessage().contains("Syntax error in query"),
                "Exception message should contain the job error");
    }

    @Test
    void getQueryResult_whenJobStatusIsCancelled_throwsRuntimeException() {
        Long queryId = 1L;
        setupExecuteQueryResponse(queryId, "job-1");

        RedashQueryResponse cancelledResponse = RedashQueryResponse.builder()
                .job(RedashQueryResponse.Job.builder()
                        .id("job-1").status(5L)
                        .build())
                .build();
        when(redashClient.getJobStatus("job-1")).thenReturn(cancelledResponse);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redashService.getQueryResult(queryId, Map.of()),
                "Should throw when job status indicates cancellation");
        assertTrue(ex.getMessage().contains("cancelled"),
                "Exception message should indicate cancellation");
    }

    @Test
    void getQueryResult_whenJobStatusResponseIsNull_throwsRuntimeException() {
        Long queryId = 1L;
        setupExecuteQueryResponse(queryId, "job-1");
        when(redashClient.getJobStatus("job-1")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redashService.getQueryResult(queryId, Map.of()),
                "Should throw when job status response is null");
        assertTrue(ex.getMessage().contains("Failed to get job status"),
                "Exception message should indicate failed job status retrieval");
    }

    @Test
    void getQueryResult_whenJobSucceedsButResultIdMissing_throwsRuntimeException() {
        Long queryId = 1L;
        setupExecuteQueryResponse(queryId, "job-1");

        RedashQueryResponse successNoResultId = RedashQueryResponse.builder()
                .job(RedashQueryResponse.Job.builder()
                        .id("job-1").status(3L).queryResultId(null)
                        .build())
                .build();
        when(redashClient.getJobStatus("job-1")).thenReturn(successNoResultId);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redashService.getQueryResult(queryId, Map.of()),
                "Should throw when job succeeds but query_result_id is missing");
        assertTrue(ex.getMessage().contains("query_result_id is missing"),
                "Exception message should indicate missing result id");
    }

    // ── getQueryResult: FeignException handling ──

    @Test
    void getQueryResult_whenFeignExceptionWithParsableJobError_throwsWithExtractedMessage() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn("{\"job\":{\"error\":\"Permission denied\"}}");
        when(redashClient.executeQuery(eq(1L), any())).thenThrow(feignException);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redashService.getQueryResult(1L, Map.of()),
                "Should throw RuntimeException wrapping extracted Feign error");
        assertEquals("Permission denied", ex.getMessage(),
                "Exception message should be the extracted error from Feign response");
    }

    @Test
    void getQueryResult_whenFeignExceptionWithUnparsableBody_throwsDefaultMessage() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn("not-json");
        when(redashClient.executeQuery(eq(1L), any())).thenThrow(feignException);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redashService.getQueryResult(1L, Map.of()),
                "Should throw RuntimeException with default message for unparsable body");
        assertEquals("Failed to execute Redash query", ex.getMessage(),
                "Should use default fallback error message when body is not parsable");
    }

    // ── getQueryResultRows ──

    @Test
    void getQueryResultRows_whenQuerySucceeds_returnsAllRows() {
        Long queryId = 1L;
        setupSuccessfulPolling(queryId, "job-1", "result-1");
        when(redashClient.downloadQueryResultAsJson("result-1"))
                .thenReturn("{\"query_result\":{\"data\":{\"rows\":[{\"a\":1},{\"a\":2},{\"a\":3}]}}}");

        List<Map<String, Object>> result = redashService.getQueryResultRows(queryId, Map.of());

        assertEquals(3, result.size(), "Should return all rows from the query result");
    }

    @Test
    void getQueryResultRows_whenFeignExceptionOccurs_throwsRuntimeException() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn("");
        when(redashClient.executeQuery(eq(1L), any())).thenThrow(feignException);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> redashService.getQueryResultRows(1L, Map.of()),
                "Should throw RuntimeException when Feign client fails");
        assertEquals("Failed to execute Redash query", ex.getMessage(),
                "Should use default fallback error message");
    }

    // ── generateReport ──

    @Test
    void generateReport_whenQuerySucceeds_returnsResponse() throws Exception {
        Long queryId = 1L;
        RedashReportRequest request = RedashReportRequest.builder()
                .queryId(queryId).fileType(FileType.CSV).parameters(Map.of()).build();

        setupSuccessfulPolling(queryId, "job-1", "result-1");
        Response mockResponse = mock(Response.class);
        when(redashClient.downloadQueryResult("result-1", "csv")).thenReturn(mockResponse);

        CompletableFuture<Response> future = redashService.generateReport(request);
        Response result = future.get(10, TimeUnit.SECONDS);

        assertSame(mockResponse, result, "Should return the downloaded file response");
    }

    @Test
    void generateReport_whenExecuteQueryReturnsNull_throwsExecutionException() {
        RedashReportRequest request = RedashReportRequest.builder()
                .queryId(1L).fileType(FileType.CSV).parameters(Map.of()).build();
        when(redashClient.executeQuery(eq(1L), any())).thenReturn(null);

        CompletableFuture<Response> future = redashService.generateReport(request);

        ExecutionException ex = assertThrows(ExecutionException.class,
                () -> future.get(10, TimeUnit.SECONDS),
                "Should throw ExecutionException when executeQuery returns null");
        assertInstanceOf(RuntimeException.class, ex.getCause(),
                "Cause should be RuntimeException for invalid Redash response");
    }

    @Test
    void generateReport_whenDownloadReturnsNull_throwsExecutionException() {
        Long queryId = 1L;
        RedashReportRequest request = RedashReportRequest.builder()
                .queryId(queryId).fileType(FileType.CSV).parameters(Map.of()).build();

        setupSuccessfulPolling(queryId, "job-1", "result-1");
        when(redashClient.downloadQueryResult("result-1", "csv")).thenReturn(null);

        CompletableFuture<Response> future = redashService.generateReport(request);

        ExecutionException ex = assertThrows(ExecutionException.class,
                () -> future.get(10, TimeUnit.SECONDS),
                "Should throw ExecutionException when file download returns null");
        assertTrue(ex.getCause().getMessage().contains("Failed to download"),
                "Cause message should indicate download failure");
    }

    // ── generateExcelReport: validation ──

    @Test
    void generateExcelReport_whenRequestIsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> redashService.generateExcelReport(null),
                "Should throw IllegalArgumentException for null request");
    }

    @Test
    void generateExcelReport_whenQueryIdsIsNull_throwsIllegalArgumentException() {
        RedashExcelReportRequest request = RedashExcelReportRequest.builder()
                .queryIds(null).build();

        assertThrows(IllegalArgumentException.class,
                () -> redashService.generateExcelReport(request),
                "Should throw IllegalArgumentException when queryIds is null");
    }

    @Test
    void generateExcelReport_whenQueryIdsIsEmpty_throwsIllegalArgumentException() {
        RedashExcelReportRequest request = RedashExcelReportRequest.builder()
                .queryIds(Collections.emptyList()).build();

        assertThrows(IllegalArgumentException.class,
                () -> redashService.generateExcelReport(request),
                "Should throw IllegalArgumentException when queryIds is empty");
    }

    // ── helpers ──

    private void setupExecuteQueryResponse(Long queryId, String jobId) {
        RedashQueryResponse executeResponse = RedashQueryResponse.builder()
                .job(RedashQueryResponse.Job.builder().id(jobId).build())
                .build();
        when(redashClient.executeQuery(eq(queryId), any())).thenReturn(executeResponse);
    }

    private void setupSuccessfulPolling(Long queryId, String jobId, String queryResultId) {
        setupExecuteQueryResponse(queryId, jobId);
        RedashQueryResponse successResponse = RedashQueryResponse.builder()
                .job(RedashQueryResponse.Job.builder()
                        .id(jobId).status(3L).queryResultId(queryResultId)
                        .build())
                .build();
        when(redashClient.getJobStatus(jobId)).thenReturn(successResponse);
    }
}
