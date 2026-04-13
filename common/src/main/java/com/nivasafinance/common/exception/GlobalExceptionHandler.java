package com.nivasafinance.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.NOT_FOUND);
        apiError.setErrorCode("RESOURCE_NOT_FOUND");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationException(
            ValidationException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.BAD_REQUEST);
        apiError.setErrorCode("VALIDATION_ERROR");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.BAD_REQUEST);
        apiError.setErrorCode("BAD_REQUEST");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(
            ConflictException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.CONFLICT);
        apiError.setErrorCode("CONFLICT");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiError> handleResourceConflictException(
            ResourceConflictException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.CONFLICT);
        apiError.setErrorCode("RESOURCE_CONFLICT");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.UNAUTHORIZED);
        apiError.setErrorCode("UNAUTHORIZED");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbiddenException(
            ForbiddenException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.FORBIDDEN);
        apiError.setErrorCode("FORBIDDEN");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage() != null 
                    ? error.getDefaultMessage() 
                    : "Validation failed.")
        );
        
        ApiError apiError = new ApiError();
        apiError.setFieldErrors(errors);
        apiError.setStatusCode(HttpStatus.BAD_REQUEST);
        apiError.setErrorCode("VALIDATION_ERROR");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        logger.error("Invalid JSON format - RequestId: {}, Path: {}, Error: {}", 
                requestId, path, ex.getMessage(), ex);
        ApiError apiError = new ApiError();
        apiError.setError("Invalid JSON format: " + ex.getMessage());
        apiError.setStatusCode(HttpStatus.BAD_REQUEST);
        apiError.setErrorCode("INVALID_JSON");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiError> handleInvalidFormatException(
            InvalidFormatException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        logger.error("Invalid format - RequestId: {}, Path: {}, Error: {}", 
                requestId, path, ex.getMessage(), ex);
        ApiError apiError = new ApiError();
        apiError.setError("Invalid format: " + ex.getMessage());
        apiError.setStatusCode(HttpStatus.BAD_REQUEST);
        apiError.setErrorCode("INVALID_FORMAT");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        String parameterName = ex.getName();
        String errorMessage = String.format("Invalid value for parameter '%s': %s. Expected type: %s", 
                parameterName, ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        
        logger.warn("Invalid path parameter - RequestId: {}, Path: {}, Parameter: {}, Value: {}, Error: {}", 
                requestId, path, parameterName, ex.getValue(), ex.getMessage());
        
        ApiError apiError = new ApiError();
        apiError.setError(errorMessage);
        apiError.setStatusCode(HttpStatus.BAD_REQUEST);
        apiError.setErrorCode("INVALID_PATH_PARAMETER");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        HttpStatusCode statusCode = ex.getStatusCode();
        HttpStatus resolved = HttpStatus.resolve(statusCode.value());
        if (resolved == null) {
            if (statusCode.is4xxClientError()) {
                resolved = HttpStatus.BAD_REQUEST;
            } else if (statusCode.is5xxServerError()) {
                resolved = HttpStatus.BAD_GATEWAY;
            } else {
                resolved = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        String message = ex.getReason();
        if (message == null || message.isBlank()) {
            message = resolved.getReasonPhrase();
        }
        ApiError apiError = new ApiError();
        apiError.setError(message);
        apiError.setStatusCode(resolved);
        apiError.setErrorCode("RESPONSE_STATUS");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(statusCode).body(apiError);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        String requestId = generateRequestId();
        String path = request.getDescription(false);
        logger.error("Runtime exception - RequestId: {}, Path: {}, Error: {}", 
                requestId, path, ex.getLocalizedMessage(), ex);
        logger.error("Stack trace: {}", (Object) ex.getStackTrace());
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        apiError.setErrorCode("INTERNAL_ERROR");
        apiError.setRequestId(requestId);
        apiError.setPath(path);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}

