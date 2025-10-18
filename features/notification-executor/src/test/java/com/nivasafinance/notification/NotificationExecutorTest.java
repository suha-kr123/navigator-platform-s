package com.nivasafinance.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for NotificationExecutor.
 * This demonstrates that the module is using pure Java.
 */
public class NotificationExecutorTest {
    
    private NotificationExecutor notificationExecutor;
    
    @BeforeEach
    public void setUp() {
        notificationExecutor = new NotificationExecutor();
    }
    
    @Test
    public void testExecuteNotificationWithValidInputs() {
        // Given
        String notificationType = "TEST";
        String message = "This is a test notification";
        
        // When
        boolean result = notificationExecutor.executeNotification(notificationType, message);
        
        // Then
        assertTrue(result, "Notification should be executed successfully");
    }
    
    @Test
    public void testExecuteNotificationWithNullType() {
        // Given
        String notificationType = null;
        String message = "This is a test notification";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            notificationExecutor.executeNotification(notificationType, message);
        });
    }
    
    @Test
    public void testExecuteNotificationWithNullMessage() {
        // Given
        String notificationType = "TEST";
        String message = null;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            notificationExecutor.executeNotification(notificationType, message);
        });
    }
    
    @Test
    public void testSendEmailNotification() {
        // Given
        String recipient = "test@example.com";
        String subject = "Test Subject";
        String body = "Test email body";
        
        // When
        boolean result = notificationExecutor.sendEmailNotification(recipient, subject, body);
        
        // Then
        assertTrue(result, "Email notification should be sent successfully");
    }
    
    @Test
    public void testSendSmsNotification() {
        // Given
        String phoneNumber = "+1234567890";
        String message = "Test SMS message";
        
        // When
        boolean result = notificationExecutor.sendSmsNotification(phoneNumber, message);
        
        // Then
        assertTrue(result, "SMS notification should be sent successfully");
    }
    
    @Test
    public void testSendPushNotification() {
        // Given
        String deviceToken = "device-token-123";
        String title = "Test Push";
        String message = "Test push message";
        
        // When
        boolean result = notificationExecutor.sendPushNotification(deviceToken, title, message);
        
        // Then
        assertTrue(result, "Push notification should be sent successfully");
    }
}
