package com.nivasafinance.common.messaging.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueueType {
    NOTIFICATION("notification"),
    NOTIFICATION_EXECUTOR("notification-executor"),
    AUDIT("audit");

    private final String propertyKey;
}