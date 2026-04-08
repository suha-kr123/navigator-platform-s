package com.nivasafinance.features.bre.controller;

import com.nivasafinance.features.bre.dto.BREExecutionRequest;
import com.nivasafinance.features.bre.dto.BREExecutionResponse;
import com.nivasafinance.features.bre.service.BREExecutionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BREExecutionControllerTest {

    @Mock
    private BREExecutionService breExecutionService;

    @InjectMocks
    private BREExecutionController controller;

    // ── execute ──

    @Test
    void execute_withRequest_delegatesToServiceAndReturnsOk() throws Exception {
        BREExecutionRequest request = BREExecutionRequest.builder()
                .customData("some-data")
                .params(Map.of("k", "v"))
                .build();
        BREExecutionResponse expected = BREExecutionResponse.builder()
                .logId(1L)
                .response(Map.of("score", 100))
                .build();
        when(breExecutionService.execute("my-cfg", request))
                .thenReturn(CompletableFuture.completedFuture(expected));

        CompletableFuture<ResponseEntity<BREExecutionResponse>> result =
                controller.execute("my-cfg", request);

        ResponseEntity<BREExecutionResponse> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return 200 OK on successful execution");
        assertEquals(1L, response.getBody().getLogId(),
                "Response should contain the execution log ID");
        verify(breExecutionService).execute("my-cfg", request);
    }

    @Test
    void execute_withNullRequest_passesNewEmptyRequest() throws Exception {
        BREExecutionResponse expected = BREExecutionResponse.builder().logId(2L).build();
        when(breExecutionService.execute(eq("cfg"), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(expected));

        CompletableFuture<ResponseEntity<BREExecutionResponse>> result =
                controller.execute("cfg", null);

        ResponseEntity<BREExecutionResponse> response = result.get();
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return 200 OK even when request body is null");
        assertEquals(2L, response.getBody().getLogId(),
                "Response should contain the log ID from execution");
    }
}
