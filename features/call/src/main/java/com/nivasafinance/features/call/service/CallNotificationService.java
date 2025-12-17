package com.nivasafinance.features.call.service;

import com.nivasafinance.webhooks.call.dto.CallNotificationResponse;

import java.util.List;

public interface CallNotificationService {
    List<CallNotificationResponse> getNotificationsForCurrentUser();
}

