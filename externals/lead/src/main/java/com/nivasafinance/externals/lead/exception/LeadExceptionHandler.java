package com.nivasafinance.externals.lead.exception;

import com.nivasafinance.common.exception.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class LeadExceptionHandler {

    @ExceptionHandler(ActiveLeadAlreadyExistsWithDetailsException.class)
    public ResponseEntity<ApiError> handleActiveLeadAlreadyExistsWithDetailsException(
            ActiveLeadAlreadyExistsWithDetailsException ex,
            WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        String path = request.getDescription(false);
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.CONFLICT);
        apiError.setErrorCode("CONFLICT");
        apiError.setRequestId(requestId);
        apiError.setPath(path);

        // Add additional data
        Map<String, Object> additionalData = new HashMap<>();
        if (ex.getLeadIdentifier() != null) {
            additionalData.put("leadIdentifier", ex.getLeadIdentifier().toString());
        }
        if (ex.getAddress() != null) {
            additionalData.put("address", ex.getAddress());
        }
        if (ex.getPreliminaryDetails() != null) {
            additionalData.put("preliminaryDetails", ex.getPreliminaryDetails());
        }
        if (ex.getStatus() != null) {
            additionalData.put("status", ex.getStatus().toString());
        }
        if (ex.getStage() != null) {
            additionalData.put("stage", ex.getStage());
        }
        apiError.setAdditionalData(additionalData);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }
}
