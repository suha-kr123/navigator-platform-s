package com.nivasafinance.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for notification executor module.
 * This demonstrates Spring integration in a pure Java module.
 */
@Configuration
public class NotificationConfig {
    
    /**
     * Creates a notification executor bean.
     * 
     * @return a configured NotificationExecutor instance
     */
    @Bean
    public NotificationExecutor notificationExecutor() {
        return new NotificationExecutor();
    }
    
    /**
     * Creates a notification service bean.
     * 
     * @return a configured NotificationService instance
     */
    @Bean
    public NotificationService notificationService() {
        return new NotificationService(notificationExecutor());
    }
}
