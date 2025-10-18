package com.nivasafinance.notification;

import org.springframework.stereotype.Service;

/**
 * Service class for handling notification operations.
 * This demonstrates a higher-level service that uses the NotificationExecutor.
 */
@Service
public class NotificationService {
    
    private final NotificationExecutor notificationExecutor;
    
    /**
     * Constructor for NotificationService.
     * 
     * @param notificationExecutor the notification executor to use
     */
    public NotificationService(NotificationExecutor notificationExecutor) {
        this.notificationExecutor = notificationExecutor;
    }
    
    /**
     * Sends a welcome notification to a new user.
     * 
     * @param userEmail the user's email address
     * @param userName the user's name
     * @return true if the notification was sent successfully
     */
    public boolean sendWelcomeNotification(String userEmail, String userName) {
        String subject = "Welcome to Navigator Platform";
        String body = "Hello " + userName + ", welcome to the Navigator Platform!";
        
        return notificationExecutor.sendEmailNotification(userEmail, subject, body);
    }
    
    /**
     * Sends a password reset notification.
     * 
     * @param userEmail the user's email address
     * @param resetToken the password reset token
     * @return true if the notification was sent successfully
     */
    public boolean sendPasswordResetNotification(String userEmail, String resetToken) {
        String subject = "Password Reset Request";
        String body = "Please use the following token to reset your password: " + resetToken;
        
        return notificationExecutor.sendEmailNotification(userEmail, subject, body);
    }
    
    /**
     * Sends a system alert notification.
     * 
     * @param alertMessage the alert message
     * @return true if the notification was sent successfully
     */
    public boolean sendSystemAlert(String alertMessage) {
        return notificationExecutor.executeNotification("SYSTEM_ALERT", alertMessage);
    }
    
    /**
     * Sends a bulk notification to multiple recipients.
     * 
     * @param recipients array of email addresses
     * @param subject the email subject
     * @param body the email body
     * @return the number of successful notifications sent
     */
    public int sendBulkNotification(String[] recipients, String subject, String body) {
        int successCount = 0;
        
        for (String recipient : recipients) {
            if (notificationExecutor.sendEmailNotification(recipient, subject, body)) {
                successCount++;
            }
        }
        
        return successCount;
    }
}
