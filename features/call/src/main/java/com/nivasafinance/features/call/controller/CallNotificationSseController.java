package com.nivasafinance.features.call.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.dto.CallNotificationResponse;
import com.nivasafinance.common.dto.EnrichedCallNotificationResponse;
import com.nivasafinance.features.call.service.CallNotificationService;
import com.nivasafinance.features.call.service.CallNotificationSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(ApiConstants.V1 + "/notifications")
@RequiredArgsConstructor
@Slf4j
public class CallNotificationSseController {

    private final CallNotificationSseService sseService;
    private final CallNotificationService notificationService;

    @GetMapping(value = "/call-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToCallNotifications(HttpServletResponse response) {
        // Set headers explicitly to improve HTTP/2 and gateway compatibility
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // Prevent caching and transformation at gateway/proxy level
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Disable buffering at nginx/proxy level - critical for SSE streaming
        response.setHeader("X-Accel-Buffering", "no");
        
        // Security headers
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // Gateway/Proxy specific headers to ensure streaming works
        // These help prevent gateways from buffering or closing the connection
        response.setHeader("X-Accel-No-Buffering", "yes"); // Alternative nginx header
        // Note: Transfer-Encoding is set automatically by Spring/Tomcat for SSE
        
        // Note: Connection header is not used in HTTP/2, removed for better compatibility
        
        return sseService.subscribeToCallNotifications();
    }

    @DeleteMapping("/call-events")
    public ResponseEntity<Void> unsubscribeFromCallNotifications() {
        sseService.unsubscribeFromCallNotifications();
        return ResponseEntity.noContent().build();
    }

    /**
     * Get call notifications from call logs for the current user with pagination.
     * Includes lead and advisor information if the call is linked to them.
     * Can return multiple leads/advisors for the same phone number.
     */
    @GetMapping("/call-notifications")
    public ResponseEntity<PaginatedResponse<EnrichedCallNotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        PaginationRequest paginationRequest = new PaginationRequest(offset, limit, "created_at", "DESC");
        PaginatedResponse<EnrichedCallNotificationResponse> response = notificationService.getNotificationsForCurrentUser(paginationRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Get the most recent call notification from Redis for the current user (for reconnection scenarios).
     * 
     * Used when SSE reconnects after a disconnect to catch any missed notifications.
     * Returns only the most recent notification - older ones will be available in the database.
     * 
     * @return The most recent notification from Redis, or 404 if none found
     */
    @GetMapping("/call-notifications/recent")
    public ResponseEntity<CallNotificationResponse> getRecentNotificationsFromRedis() {
        return notificationService.getRecentNotificationsFromRedis()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

