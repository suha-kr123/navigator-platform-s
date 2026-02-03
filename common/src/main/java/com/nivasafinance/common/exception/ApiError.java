package com.nivasafinance.common.exception;

import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class ApiError {
    private LocalDateTime timeStamp;
    private String error;
    private HttpStatus statusCode;
    private String errorCode;
    private Map<String, String> fieldErrors;
    private Map<String, Object> additionalData;
    private String requestId;
    private String path;

    public ApiError() {
        this.timeStamp = LocalDateTime.now();
    }

    public ApiError(LocalDateTime timeStamp, String error, HttpStatus statusCode, String errorCode,
                    Map<String, String> fieldErrors, String requestId, String path) {
        this.timeStamp = timeStamp != null ? timeStamp : LocalDateTime.now();
        this.error = error;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
        this.requestId = requestId;
        this.path = path;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiError apiError = (ApiError) o;
        return Objects.equals(timeStamp, apiError.timeStamp) &&
                Objects.equals(error, apiError.error) &&
                statusCode == apiError.statusCode &&
                Objects.equals(errorCode, apiError.errorCode) &&
                Objects.equals(fieldErrors, apiError.fieldErrors) &&
                Objects.equals(requestId, apiError.requestId) &&
                Objects.equals(path, apiError.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeStamp, error, statusCode, errorCode, fieldErrors, requestId, path);
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "timeStamp=" + timeStamp +
                ", error='" + error + '\'' +
                ", statusCode=" + statusCode +
                ", errorCode='" + errorCode + '\'' +
                ", fieldErrors=" + fieldErrors +
                ", requestId='" + requestId + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}

