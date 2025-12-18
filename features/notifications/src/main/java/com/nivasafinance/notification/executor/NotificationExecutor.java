package com.nivasafinance.notification.executor;

import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;

/**
 * Interface for notification executors that send notifications via different providers.
 * Each executor handles a specific mode (provider) and channel type combination.
 */
public interface NotificationExecutor {
    
    /**
     * Sends a notification based on the receipt.
     * 
     * @param receipt The notification receipt containing all necessary information
     * @param renderedMessage The rendered message content (template variables replaced)
     * @throws Exception if sending fails
     */
    void send(NotificationReceipt receipt, String renderedMessage) throws Exception;
    
    /**
     * Returns the mode (provider) this executor handles (e.g., "WATI", "TWILIO", "GMAIL").
     */
    String getMode();
    
    /**
     * Returns the channel type this executor handles (e.g., "WHATSAPP", "SMS", "EMAIL").
     */
    String getChannelType();
}

