package com.nivasafinance.features.call.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.services.voice.webhook.dto.CallNotificationResponse;
import com.nivasafinance.features.call.service.CallNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/call-notifications")
@RequiredArgsConstructor
public class CallNotificationController {

    private final CallNotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<CallNotificationResponse>> getNotifications() {
        List<CallNotificationResponse> notifications = notificationService.getNotificationsForCurrentUser();
        return ResponseEntity.ok(notifications);
    }
}

