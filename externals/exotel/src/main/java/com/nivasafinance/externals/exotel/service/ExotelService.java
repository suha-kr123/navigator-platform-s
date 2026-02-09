package com.nivasafinance.externals.exotel.service;

/**
 * Service interface for processing Exotel webhooks and external events.
 */
public interface ExotelService {
    
    /**
     * Process a missed call event from Exotel webhook.
     * This method fetches call details from Exotel, searches or creates a lead,
     * and logs the call information.
     * 
     * Note: UserContext (username) will be automatically propagated to the async thread
     * via UserContextTaskDecorator configured in the async executor.
     * 
     * @param callSid The Exotel call SID from the webhook
     */
    void processMissedCall(String callSid, String customField);
    
    /**
     * Process an answered call event from Exotel webhook.
     * This method fetches call details from Exotel, searches for an existing lead,
     * and logs the call information. Unlike processMissedCall, this does NOT create
     * a new lead if one doesn't exist.
     * 
     * Note: UserContext (username) will be automatically propagated to the async thread
     * via UserContextTaskDecorator configured in the async executor.
     * 
     * @param callSid The Exotel call SID from the webhook
     */
    void processAnsweredCall(String callSid, String customField);

    /**
     * Process a campaign call status event.
     * Checks for duplicate logs, fetches call details from Exotel,
     * and logs the call for existing leads only.
     *
     * Note: UserContext (username) will be automatically propagated to the async thread
     * via UserContextTaskDecorator configured in the async executor.
     *
     * @param campaignId The campaign identifier (UUID string or provider ID)
     * @param callSid The Exotel call SID
     */
    void processCampaignCallStatus(String campaignId, String callSid);

    /**
     * Process an outgoing call callback event.
     * Fetches call details from Exotel using the callId,
     * and updates the call log for the associated lead or advisor.
     *
     * Note: UserContext (username) will be automatically propagated to the async thread
     * via UserContextTaskDecorator configured in the async executor.
     *
     * @param callId The Exotel call SID
     */
    void processOutgoingCallback(String callId);

    /**
     * Process a campaign callback event.
     * Fetches campaign details using the provider ID (campaign_sid),
     * and triggers a refresh of the campaign.
     *
     * Note: UserContext (username) will be automatically propagated to the async thread
     * via UserContextTaskDecorator configured in the async executor.
     *
     * @param campaignSid The campaign provider ID (campaign_sid)
     */
    void processCampaignCallback(String campaignSid);

    /**
     * Process a missed call event for advisor from Exotel webhook.
     * This method fetches call details from Exotel, searches or creates an advisor,
     * and logs the call information.
     * 
     * Note: UserContext (username) will be automatically propagated to the async thread
     * via UserContextTaskDecorator configured in the async executor.
     * 
     * @param callSid The Exotel call SID from the webhook
     */
    void processAdvisorMissedCall(String callSid);

    /**
     * Process an answered call event for advisor from Exotel webhook.
     * This method fetches call details from Exotel, searches for an existing advisor,
     * and logs the call information. Unlike processAdvisorMissedCall, this does NOT create
     * a new advisor if one doesn't exist.
     * 
     * Note: UserContext (username) will be automatically propagated to the async thread
     * via UserContextTaskDecorator configured in the async executor.
     * 
     * @param callSid The Exotel call SID from the webhook
     */
    void processAdvisorAnsweredCall(String callSid);
}
