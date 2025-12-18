package com.nivasafinance.notification.executor.factory;

import com.nivasafinance.notification.executor.NotificationExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for creating notification executors based on mode and channel type.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationExecutorFactory {

    private final List<NotificationExecutor> executors;

    /**
     * Gets an executor for the specified mode and channel type.
     * 
     * @param mode The provider mode (e.g., "WATI", "TWILIO", "GMAIL")
     * @param channelType The channel type (e.g., "WHATSAPP", "SMS", "EMAIL")
     * @return The matching executor
     * @throws IllegalStateException if no executor is found for the combination
     */
    public NotificationExecutor getExecutor(String mode, String channelType) {
        String normalizedMode = mode != null ? mode.toUpperCase() : null;
        String normalizedChannelType = channelType != null ? channelType.toUpperCase() : null;

        return executors.stream()
                .filter(executor -> executor.getMode().equals(normalizedMode) 
                        && executor.getChannelType().equals(normalizedChannelType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No executor found for mode=%s, channelType=%s", mode, channelType)));
    }
}

