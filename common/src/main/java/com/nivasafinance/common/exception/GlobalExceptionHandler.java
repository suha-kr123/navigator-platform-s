package com.nivasafinance.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.NOT_FOUND);
        apiError.setErrorCode("RESOURCE_NOT_FOUND");
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationException(
            ValidationException ex, WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.BAD_REQUEST);
        apiError.setErrorCode("VALIDATION_ERROR");
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.BAD_REQUEST);
        apiError.setErrorCode("BAD_REQUEST");
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(
            ConflictException ex, WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.CONFLICT);
        apiError.setErrorCode("CONFLICT");
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiError> handleResourceConflictException(
            ResourceConflictException ex, WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.CONFLICT);
        apiError.setErrorCode("RESOURCE_CONFLICT");
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.UNAUTHORIZED);
        apiError.setErrorCode("UNAUTHORIZED");
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbiddenException(
            ForbiddenException ex, WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.FORBIDDEN);
        apiError.setErrorCode("FORBIDDEN");
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
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
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.setError("Invalid JSON format: " + ex.getMessage());
        apiError.setStatusCode(HttpStatus.BAD_REQUEST);
        apiError.setErrorCode("INVALID_JSON");
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiError> handleInvalidFormatException(
            InvalidFormatException ex, WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.setError("Invalid format: " + ex.getMessage());
        apiError.setStatusCode(HttpStatus.BAD_REQUEST);
        apiError.setErrorCode("INVALID_FORMAT");
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        ApiError apiError = new ApiError();
        apiError.setError(ex.getLocalizedMessage());
        apiError.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        apiError.setErrorCode("INTERNAL_ERROR");
        apiError.setRequestId(generateRequestId());
        apiError.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}

