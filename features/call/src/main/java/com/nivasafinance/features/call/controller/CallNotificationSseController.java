package com.nivasafinance.features.call.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.dto.CallNotificationResponse;
import com.nivasafinance.features.call.service.CallNotificationService;
import com.nivasafinance.features.call.service.CallNotificationSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

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
     * Get stored notifications for the current user (for initial load or missed notifications)
     */
    @GetMapping("/call-notifications")
    public ResponseEntity<List<CallNotificationResponse>> getNotifications() {
        List<CallNotificationResponse> notifications = notificationService.getNotificationsForCurrentUser();
        return ResponseEntity.ok(notifications);
    }
}

