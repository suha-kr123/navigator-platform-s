package com.nivasafinance.notification.orchestrator.enums;

/**
 * Status of WhatsApp message delivery and interaction.
 */
public enum WhatsAppMessageStatus {
    SENT,           // Message was sent successfully
    DELIVERED,      // Message was delivered to recipient
    FAILED,         // Message failed to send
    REPLIED         // Template message was replied to
}

