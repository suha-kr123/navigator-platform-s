package com.nivasafinance.common.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.audit.ApiAuditLog;
import com.nivasafinance.common.audit.AuditAspect;
import com.nivasafinance.common.audit.AuditConfig;
import com.nivasafinance.common.service.ApiAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class ApiAuditFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiAuditFilter.class);
    private final ApiAuditService apiAuditService;
    private final ObjectMapper objectMapper;

    public ApiAuditFilter(ApiAuditService apiAuditService) {
        this.apiAuditService = apiAuditService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // Check if this request should be completely skipped from audit
        AuditConfig auditConfig = getAuditConfigForRequest(request);
        if (auditConfig != null && auditConfig.isSkipAudit()) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        long startTime = System.currentTimeMillis();

        String errorMessage = null;
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (IOException ex) {
            errorMessage = ex.getMessage() != null ? ex.getMessage() : "IO error";
            logger.error("IOException in API filter: " + ex.getMessage(), ex);
            throw ex;
        } catch (ServletException ex) {
            errorMessage = ex.getMessage() != null ? ex.getMessage() : "Servlet error";
            logger.error("ServletException in API filter: " + ex.getMessage(), ex);
            throw ex;
        } finally {
            logAudit(wrappedRequest, wrappedResponse, startTime, errorMessage, auditConfig);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logAudit(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            long startTime,
            String errorMessage,
            AuditConfig auditConfig) {
        long duration = System.currentTimeMillis() - startTime;
        String requestBody = new String(request.getContentAsByteArray());
        String responseBody = new String(response.getContentAsByteArray());
        String username = extractUsernameFromJwt(request);
        if (username == null) {
            username = "system";
        }

        boolean shouldIgnoreResponse = auditConfig != null && auditConfig.isIgnoreResponse();

        ApiAuditLog auditLog = new ApiAuditLog();
        auditLog.setUsername(username);
        auditLog.setMethod(request.getMethod());
        auditLog.setUri(request.getRequestURI());
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setRequestBody(requestBody);
        auditLog.setResponseBody(shouldIgnoreResponse ? null : responseBody);
        auditLog.setResponseStatus(response.getStatus());
        auditLog.setErrorMessage(errorMessage != null ? errorMessage : extractErrorMessageFromResponse(responseBody));
        auditLog.setDurationMs(duration);
        auditLog.setTimestamp(LocalDateTime.now());

        apiAuditService.saveAuditLog(auditLog);
    }

    private AuditConfig getAuditConfigForRequest(HttpServletRequest request) {
        // First try to get by URI pattern
        return AuditAspect.getAuditConfigByUri(request.getRequestURI());
    }

    public String extractUsernameFromJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7); // Remove "Bearer " prefix
                DecodedJWT jwt = JWT.decode(token);
                return jwt.getClaim("username").asString();
            } catch (JWTDecodeException ex) {
                logger.warn("Invalid JWT token: " + ex.getMessage(), ex);
            }
        }

        return null;
    }

    private String extractErrorMessageFromResponse(String responseBody) {
        try {
            JsonNode json = objectMapper.readTree(responseBody);
            JsonNode errorNode = json.get("error");
            return errorNode != null ? errorNode.asText() : null;
        } catch (JsonProcessingException ex) {
            logger.debug("Invalid JSON: " + ex.getMessage(), ex);
            return null;
        }
    }
}

