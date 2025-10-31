package com.nivasafinance.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "api_audit_log")
public class ApiAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username")
    private String username;

    @Column(name = "method")
    private String method;

    @Column(name = "uri")
    private String uri;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "response_status")
    private int responseStatus;

    @Column(name = "error_message", nullable = true)
    private String errorMessage;

    @Column(name = "duration_ms")
    private long durationMs;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public ApiAuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiAuditLog(UUID id, String username, String method, String uri, String ipAddress, 
                       String userAgent, String requestBody, String responseBody, 
                       int responseStatus, String errorMessage, long durationMs, LocalDateTime timestamp) {
        this.id = id;
        this.username = username;
        this.method = method;
        this.uri = uri;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.responseStatus = responseStatus;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiAuditLog that = (ApiAuditLog) o;
        return responseStatus == that.responseStatus &&
                durationMs == that.durationMs &&
                Objects.equals(id, that.id) &&
                Objects.equals(username, that.username) &&
                Objects.equals(method, that.method) &&
                Objects.equals(uri, that.uri) &&
                Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(userAgent, that.userAgent) &&
                Objects.equals(requestBody, that.requestBody) &&
                Objects.equals(responseBody, that.responseBody) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, method, uri, ipAddress, userAgent, 
                requestBody, responseBody, responseStatus, errorMessage, durationMs, timestamp);
    }

    @Override
    public String toString() {
        return "ApiAuditLog{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", responseBody='" + responseBody + '\'' +
                ", responseStatus=" + responseStatus +
                ", errorMessage='" + errorMessage + '\'' +
                ", durationMs=" + durationMs +
                ", timestamp=" + timestamp +
                '}';
    }
}

