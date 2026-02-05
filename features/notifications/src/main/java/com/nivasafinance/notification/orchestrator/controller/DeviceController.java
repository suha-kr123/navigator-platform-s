package com.nivasafinance.notification.orchestrator.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.notification.orchestrator.dto.RegisterDeviceRequest;
import com.nivasafinance.notification.orchestrator.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/users")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;

    /** Register/update FCM token for app user; path username must match authenticated user. */
    @PostMapping("/{username}/notification-tokens")
    @RequirePermission(permissionName = "REGISTER_DEVICE")
    public ResponseEntity<Void> registerDevice(
            @PathVariable String username,
            @Valid @RequestBody RegisterDeviceRequest request) {
        String currentUsername = UserContext.getUsername();
        if (currentUsername == null || !currentUsername.equals(username)) {
            log.warn("User {} attempted to register device for different app user {}", currentUsername, username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        deviceService.registerDeviceForUser(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
