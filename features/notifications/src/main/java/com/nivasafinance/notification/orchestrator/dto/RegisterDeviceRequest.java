package com.nivasafinance.notification.orchestrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request to register/update FCM token for an app user. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeviceRequest {

    @NotBlank(message = "Notification token is required")
    @Size(max = 500, message = "Notification token must not exceed 500 characters")
    private String notificationToken;

    @Size(max = 255, message = "Device ID must not exceed 255 characters")
    private String deviceId;

    @NotBlank(message = "Platform is required")
    @Pattern(regexp = "(?i)^(ANDROID|IOS|WEB)$", message = "Platform must be ANDROID, IOS, or WEB")
    private String platform;

    @Size(max = 50, message = "App version must not exceed 50 characters")
    private String appVersion;

    @Size(max = 50, message = "OS version must not exceed 50 characters")
    private String osVersion;

    @Size(max = 100, message = "Device model must not exceed 100 characters")
    private String deviceModel;

    @Size(max = 50, message = "APK version must not exceed 50 characters")
    private String apkVersion;

    @Size(max = 50, message = "SDK version must not exceed 50 characters")
    private String sdkVersion;
}

