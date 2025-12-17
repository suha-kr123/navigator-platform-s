package com.nivasafinance.features.call.service;

import com.nivasafinance.services.voice.webhook.dto.CallNotificationResponse;

import java.util.List;

public interface CallNotificationService {
    List<CallNotificationResponse> getNotificationsForCurrentUser();
}

