package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.notification.orchestrator.dto.RegisterDeviceRequest;

import java.util.List;

public interface DeviceService {

    /** Register or update device token for app user (upsert by deviceId or token). */
    void registerDeviceForUser(String appUser, RegisterDeviceRequest request);

    /** Active FCM tokens for app user (used by Firebase executor). */
    List<String> getActiveNotificationTokensForUser(String appUser);

    /** Deactivate devices by invalid tokens (e.g. after FCM UNREGISTERED). */
    int deactivateDevicesByTokens(String appUser, List<String> notificationTokens);
}
