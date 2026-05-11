package com.nivasafinance.features.call.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Generates a unique instance ID at startup.
 * Used to identify the source of Redis Pub/Sub messages
 * and prevent duplicate delivery on the publishing instance.
 */
@Component
@Slf4j
@Getter
public class CallNotificationInstanceId {

    private final String id = UUID.randomUUID().toString();

    public CallNotificationInstanceId() {
        log.info("Call notification instance ID: {}", id);
    }
}
