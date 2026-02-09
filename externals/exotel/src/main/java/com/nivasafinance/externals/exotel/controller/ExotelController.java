package com.nivasafinance.externals.exotel.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.externals.exotel.dto.CampaignCallStatusRequest;
import com.nivasafinance.externals.exotel.dto.CampaignCallbackRequest;
import com.nivasafinance.externals.exotel.dto.OutgoingCallbackRequest;
import com.nivasafinance.externals.exotel.service.ExotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(ApiConstants.EXTERNAL_V1 + "/exotel")
@RequiredArgsConstructor
@Slf4j
public class ExotelController {

    private final ExotelService exotelService;

    /**
     * Webhook endpoint for Exotel missed call events.
     * Accepts CallSid as a query parameter and processes the call asynchronously.
     *
     * @param callSid The Exotel Call SID from the webhook
     * @return 200 OK with a message indicating the request is being processed
     */
    @GetMapping("/lead/incoming/missed-call")
    public ResponseEntity<Map<String, String>> handleMissedCall(
            @RequestParam(value = "CallSid", required = false) String callSid
    ) {
        log.info("Received missed call webhook with CallSid: {}", callSid);

        // Validate CallSid parameter
        if (callSid == null || callSid.isBlank()) {
            log.warn("Missing or blank CallSid parameter in missed call webhook");
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "CallSid parameter is required"));
        }

        // Start async processing
        exotelService.processMissedCall(callSid);

        // Return immediate response
        return ResponseEntity.ok()
                .body(Map.of("message", "Call logging request received and processing in background"));
    }

    /**
     * Webhook endpoint for Exotel answered call events.
     * Accepts CallSid as a query parameter and processes the call asynchronously.
     * Unlike missed-call, this endpoint only logs calls for existing leads and does NOT create new leads.
     *
     * @param callSid The Exotel Call SID from the webhook
     * @return 200 OK with a message indicating the request is being processed
     */
    @GetMapping("/lead/incoming/answered")
    public ResponseEntity<Map<String, String>> handleAnsweredCall(
            @RequestParam(value = "CallSid", required = false) String callSid
    ) {
        log.info("Received answered call webhook with CallSid: {}", callSid);

        // Validate CallSid parameter
        if (callSid == null || callSid.isBlank()) {
            log.warn("Missing or blank CallSid parameter in answered call webhook");
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "CallSid parameter is required"));
        }

        // Start async processing - UserContext will be automatically propagated
        exotelService.processAnsweredCall(callSid);

        // Return immediate response
        return ResponseEntity.ok()
                .body(Map.of("message", "Call logging request received and processing in background"));
    }

    /**
     * Webhook endpoint for Exotel campaign call status events.
     * Accepts JSON body with campaign_sid and call_sid.
     * Checks for duplicate logs, fetches call details from Exotel,
     * and logs calls for existing leads only.
     *
     * @param request Request body containing campaign_sid and call_sid
     * @return 200 OK with a message indicating the request is being processed
     */
    @PostMapping("/campaign/call/status")
    public ResponseEntity<Map<String, String>> handleCampaignCallStatus(
            @Valid @RequestBody CampaignCallStatusRequest request
    ) {
        log.info("Received campaign call status webhook - campaign_sid: {}, call_sid: {} from user: {}",
                request.getCampaignSid(), request.getCallSid(), UserContext.getUsername());

        exotelService.processCampaignCallStatus(request.getCampaignSid(), request.getCallSid());

        return ResponseEntity.ok()
                .body(Map.of("message", "Campaign call status request received and processing in background"));
    }

    /**
     * Webhook endpoint for Exotel outgoing call callback events.
     * Accepts JSON body with callId.
     * Fetches call details from Exotel and updates the call log for the associated lead or advisor.
     *
     * @param request Request body containing callId
     * @return 200 OK with a message indicating the request is being processed
     */
    @PostMapping("/outgoing/callback")
    public ResponseEntity<Map<String, String>> handleOutgoingCallback(
            @Valid @RequestBody OutgoingCallbackRequest request
    ) {
        log.info("Received outgoing callback webhook - callId: {} from user: {}",
                request.getCallDetails().getSid(), UserContext.getUsername());

        exotelService.processOutgoingCallback(request.getCallDetails().getSid());

        return ResponseEntity.ok()
                .body(Map.of("message", "callback request received and processing in background"));
    }

    /**
     * Webhook endpoint for campaign callback events.
     * Accepts JSON body with campaign_sid.
     * Fetches campaign details and triggers a refresh.
     *
     * @param request Request body containing campaign_sid
     * @return 200 OK with a message indicating the request is being processed
     */
    @PostMapping("/campaign/callback")
    public ResponseEntity<Map<String, String>> handleCampaignCallback(
            @Valid @RequestBody CampaignCallbackRequest request
    ) {
        log.info("Received campaign callback webhook - campaign_sid: {}, status: {} from user: {}",
                request.getCampaign_sid(), request.getStatus(), UserContext.getUsername());

        exotelService.processCampaignCallback(request.getCampaign_sid());

        return ResponseEntity.ok()
                .body(Map.of("message", "Campaign callback request received and processing in background"));
    }

    /**
     * Webhook endpoint for Exotel advisor missed call events.
     * Accepts CallSid as a query parameter and processes the call asynchronously.
     *
     * @param callSid The Exotel Call SID from the webhook
     * @return 200 OK with a message indicating the request is being processed
     */
    @GetMapping("/advisor/incoming/missed-call")
    public ResponseEntity<Map<String, String>> handleAdvisorMissedCall(
            @RequestParam(value = "CallSid", required = false) String callSid
    ) {
        log.info("Received advisor missed call webhook with CallSid: {}", callSid);

        // Validate CallSid parameter
        if (callSid == null || callSid.isBlank()) {
            log.warn("Missing or blank CallSid parameter in advisor missed call webhook");
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "CallSid parameter is required"));
        }

        // Start async processing
        exotelService.processAdvisorMissedCall(callSid);

        // Return immediate response
        return ResponseEntity.ok()
                .body(Map.of("message", "Advisor call logging request received and processing in background"));
    }

    /**
     * Webhook endpoint for Exotel advisor answered call events.
     * Accepts CallSid as a query parameter and processes the call asynchronously.
     * Unlike missed-call, this endpoint only logs calls for existing advisors and does NOT create new advisors.
     *
     * @param callSid The Exotel Call SID from the webhook
     * @return 200 OK with a message indicating the request is being processed
     */
    @GetMapping("/advisor/incoming/answered")
    public ResponseEntity<Map<String, String>> handleAdvisorAnsweredCall(
            @RequestParam(value = "CallSid", required = false) String callSid
    ) {
        log.info("Received advisor answered call webhook with CallSid: {}", callSid);

        // Validate CallSid parameter
        if (callSid == null || callSid.isBlank()) {
            log.warn("Missing or blank CallSid parameter in advisor answered call webhook");
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "CallSid parameter is required"));
        }

        // Start async processing - UserContext will be automatically propagated
        exotelService.processAdvisorAnsweredCall(callSid);

        // Return immediate response
        return ResponseEntity.ok()
                .body(Map.of("message", "Advisor call logging request received and processing in background"));
    }
}
