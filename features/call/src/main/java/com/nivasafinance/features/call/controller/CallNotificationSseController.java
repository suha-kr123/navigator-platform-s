package com.nivasafinance.features.call.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.dto.EnrichedCallNotificationResponse;
import com.nivasafinance.features.call.service.CallNotificationService;
import com.nivasafinance.features.call.service.CallNotificationSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping(ApiConstants.V1 + "/notifications")
@RequiredArgsConstructor
public class CallNotificationSseController {

    private final CallNotificationSseService sseService;
    private final CallNotificationService notificationService;

    @GetMapping(value = "/call-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToCallNotifications() {
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
}

