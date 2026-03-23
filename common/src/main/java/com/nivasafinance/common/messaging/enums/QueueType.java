package com.nivasafinance.common.messaging.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueueType {
    NOTIFICATION("notification"),
    NOTIFICATION_EXECUTOR("notification-executor"),
    BULK_OPERATION_VALIDATION("bulk-operation-validation"),
    BULK_OPERATION_PROCESSING("bulk-operation-processing"),
    NAVIGATOR_ATLAS("navigator-atlas");

    private final String propertyKey;
}