package com.nivasafinance.externals.exotel.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.enums.SystemEntities;
import com.nivasafinance.common.utils.PhoneNumberUtils;
import com.nivasafinance.externals.exotel.service.ExotelService;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.campaign.dto.CampaignDetailedResponse;
import com.nivasafinance.features.campaign.service.CampaignReadService;
import com.nivasafinance.features.campaign.service.CampaignWriteService;
import com.nivasafinance.features.lead.dto.CreateExternalCallLogRequest;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.LeadSearchRequest;
import com.nivasafinance.features.lead.dto.LeadSearchResponse;
import com.nivasafinance.features.lead.dto.UpdateCallDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateSourcingDetailsRequest;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.advisor.dto.AdvisorBasicResponse;
import com.nivasafinance.features.advisor.dto.AdvisorSearchRequest;
import com.nivasafinance.features.advisor.dto.AdvisorUpdateCallLog;
import com.nivasafinance.features.advisor.dto.CreateAdvisorRequest;
import com.nivasafinance.features.advisor.dto.MobileNumberDetails;
import com.nivasafinance.features.advisor.service.AdvisorCallWriteService;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import com.nivasafinance.features.lead.dto.LeadUpdateCallLog;
import com.nivasafinance.features.lead.service.LeadCallWriteService;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.voice.VoiceHandler;
import com.nivasafinance.services.voice.dto.VoiceGetCallStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nivasafinance.features.workflow.constants.WorkflowConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
@Slf4j
public class ExotelServiceImpl implements ExotelService {

    private final ServiceFactory<VoiceHandler> voiceServiceFactory;
    private final LeadReadService leadReadService;
    private final LeadWriteService leadWriteService;
    private final LeadCallWriteService leadCallWriteService;
    private final AdvisorReadService advisorReadService;
    private final AdvisorWriteService advisorWriteService;
    private final AdvisorCallWriteService advisorCallWriteService;
    private final CampaignReadService campaignReadService;
    private final CampaignWriteService campaignWriteService;
    private final CallReadService callReadService;
    private final ObjectMapper objectMapper;

    private static final String DIRECT_CALL_SOURCE = "DIRECT_CALL_SOURCE";
    private static final String ENTITY_TYPE_CAMPAIGN = "CAMPAIGN";

    @Async
    @Override
    public void processMissedCall(String callSid, String customField) {
        log.info("Processing missed call for CallSid: {} with user: {}", callSid, UserContext.getUsername());

        try {
            // 1. Validate callSid
            if (callSid == null || callSid.isBlank()) {
                log.error("CallSid is null or blank");
                throw new IllegalArgumentException("CallSid cannot be null or blank");
            }

            // 2. Fetch Exotel call data
            VoiceHandler voiceHandler = voiceServiceFactory.getHandler(ThirdPartyServiceList.VOICE);
            BusinessContext businessContext = new BusinessContext(
                    SystemEntities.LEAD.name(),
                    null,
                    "EXOTEL_MISSED_CALL"
            );

            VoiceGetCallStatusResponse callStatusResponse = voiceHandler.getCallStatus(callSid, businessContext);
            log.info("Fetched call status for CallSid: {}", callSid);

            // 3. Extract call_from (caller phone number)
            String callFrom = extractCallFrom(callStatusResponse);
            if (callFrom == null || callFrom.isBlank()) {
                log.error("Could not extract call_from for CallSid: {}", callSid);
                return;
            }

            // Normalize phone number (remove country code, etc.)
            String normalizedCallFrom = normalizePhoneNumber(callFrom);
            log.info("Normalized call_from: {} -> {}", callFrom, normalizedCallFrom);

            // 4. Map Exotel status and direction to CRM enums
            CallStatus callStatus = mapToCallStatus(callStatusResponse.getStatus());
            CallDirection callDirection = CallDirection.INBOUND;

            // 5. Find lead by mobile number
            LeadSearchResponse leadInfo = findOrCreateLead(normalizedCallFrom);
            if (leadInfo == null) {
                log.error("Failed to find or create lead for mobile: {}", normalizedCallFrom);
                return;
            }

            UUID leadIdentifier = leadInfo.getLeadIdentifier();
            UUID contactPersonIdentifier = leadInfo.getContactPersonIdentifier();
            long numberOfCampaignCalls = leadInfo.getNumberOfCampaignCalls() != null
                    ? leadInfo.getNumberOfCampaignCalls() 
                    : 0L;

            log.info("Found/Created lead: {} for mobile: {}", leadIdentifier, normalizedCallFrom);

            // 6. Extract campaign ID if present (from call details, then from customField)
            UUID campaignId = extractCampaignId(callStatusResponse);
            if (campaignId == null) {
                campaignId = extractCampaignIdFromCustomField(customField);
            }

            // 7. Build CreateExternalCallLogRequest
            CreateExternalCallLogRequest callLogRequest = buildCallLogRequest(
                    callStatusResponse,
                    contactPersonIdentifier,
                    callStatus,
                    callDirection,
                    normalizedCallFrom,
                    campaignId
            );

            // 8. Log the call
            leadCallWriteService.createExternalCallLog(leadIdentifier, callLogRequest);
            log.info("Created external call log for lead: {}, CallSid: {}", leadIdentifier, callSid);

            // 9. Update campaign call count if campaign is present
            if (campaignId != null) {
                updateCampaignCallCount(leadIdentifier, numberOfCampaignCalls + 1);
                log.info("Updated campaign call count for lead: {}", leadIdentifier);
            }

            log.info("Successfully processed missed call for CallSid: {}", callSid);

        } catch (Exception e) {
            log.error("Error processing missed call for CallSid: {}", callSid, e);
            // Don't rethrow - async method should handle exceptions gracefully
        }
    }

    @Async
    @Override
    public void processAnsweredCall(String callSid, String customField) {
        log.info("Processing answered call for CallSid: {} with user: {}", callSid, UserContext.getUsername());

        try {
            // 1. Validate callSid
            if (callSid == null || callSid.isBlank()) {
                log.error("CallSid is null or blank");
                throw new IllegalArgumentException("CallSid cannot be null or blank");
            }

            // 2. Fetch Exotel call data
            VoiceHandler voiceHandler = voiceServiceFactory.getHandler(ThirdPartyServiceList.VOICE);
            BusinessContext businessContext = new BusinessContext(
                    SystemEntities.LEAD.name(),
                    null,
                    "EXOTEL_ANSWERED_CALL"
            );

            VoiceGetCallStatusResponse callStatusResponse = voiceHandler.getCallStatus(callSid, businessContext);
            log.info("Fetched call status for CallSid: {}", callSid);

            // 3. Extract call_from (caller phone number)
            String callFrom = extractCallFrom(callStatusResponse);
            if (callFrom == null || callFrom.isBlank()) {
                log.error("Could not extract call_from for CallSid: {}", callSid);
                return;
            }

            // Normalize phone number (remove country code, etc.)
            String normalizedCallFrom = normalizePhoneNumber(callFrom);
            log.info("Normalized call_from: {} -> {}", callFrom, normalizedCallFrom);

            // 4. Map Exotel status and direction to CRM enums
            CallStatus callStatus = mapToCallStatus(callStatusResponse.getStatus());
            CallDirection callDirection = CallDirection.INBOUND;

            // 5. Search for existing lead by mobile number (DO NOT CREATE)
            LeadSearchResponse leadInfo = findExistingLead(normalizedCallFrom);
            if (leadInfo == null) {
                log.info("No lead found for mobile: {} - skipping call logging for answered call", normalizedCallFrom);
                return;
            }

            UUID leadIdentifier = leadInfo.getLeadIdentifier();
            UUID contactPersonIdentifier = leadInfo.getContactPersonIdentifier();
            long numberOfCampaignCalls = leadInfo.getNumberOfCampaignCalls() != null
                    ? leadInfo.getNumberOfCampaignCalls() 
                    : 0L;

            log.info("Found lead: {} for mobile: {}", leadIdentifier, normalizedCallFrom);

            // 6. Extract campaign ID if present (from call details, then from customField)
            UUID campaignId = extractCampaignId(callStatusResponse);
            if (campaignId == null) {
                campaignId = extractCampaignIdFromCustomField(customField);
            }

            // 7. Build CreateExternalCallLogRequest
            CreateExternalCallLogRequest callLogRequest = buildCallLogRequest(
                    callStatusResponse,
                    contactPersonIdentifier,
                    callStatus,
                    callDirection,
                    normalizedCallFrom,
                    campaignId
            );

            // 8. Log the call
            leadCallWriteService.createExternalCallLog(leadIdentifier, callLogRequest);
            log.info("Created external call log for lead: {}, CallSid: {}", leadIdentifier, callSid);

            // 9. Update campaign call count if campaign is present
            if (campaignId != null) {
                updateCampaignCallCount(leadIdentifier, numberOfCampaignCalls + 1);
                log.info("Updated campaign call count for lead: {}", leadIdentifier);
            }

            log.info("Successfully processed answered call for CallSid: {}", callSid);

        } catch (Exception e) {
            log.error("Error processing answered call for CallSid: {}", callSid, e);
            // Don't rethrow - async method should handle exceptions gracefully
        }
    }

    @Async
    @Override
    public void processCampaignCallStatus(String campaignId, String callSid) {
        log.info("Processing campaign call status - campaignId: {}, CallSid: {} with user: {}", 
                campaignId, callSid, UserContext.getUsername());

        try {
            // 1. Validate parameters
            if (callSid == null || callSid.isBlank()) {
                log.error("CallSid is null or blank");
                return;
            }
            if (campaignId == null || campaignId.isBlank()) {
                log.error("CampaignId is null or blank");
                return;
            }

            // 2. Check if call log already exists
            Optional<CallLogResponse> existingLog = callReadService.getCallLogByProviderId(callSid);
            if (existingLog.isPresent()) {
                log.info("Call log already present for CallSid: {} - skipping", callSid);
                return;
            }

            // 3. Fetch Exotel call data (same as answered API)
            VoiceHandler voiceHandler = voiceServiceFactory.getHandler(ThirdPartyServiceList.VOICE);
            BusinessContext businessContext = new BusinessContext(
                    SystemEntities.LEAD.name(),
                    null,
                    "EXOTEL_CAMPAIGN_CALL_STATUS"
            );

            VoiceGetCallStatusResponse callStatusResponse = voiceHandler.getCallStatus(callSid, businessContext);
            log.info("Fetched call status for CallSid: {}", callSid);

            // 4. Extract call_from
            String callFrom = extractCallFrom(callStatusResponse);
            if (callFrom == null || callFrom.isBlank()) {
                log.error("Could not extract call_from for CallSid: {}", callSid);
                return;
            }

            String normalizedCallFrom = normalizePhoneNumber(callFrom);
            log.info("Normalized call_from: {} -> {}", callFrom, normalizedCallFrom);

            // 5. Map status and direction
            CallStatus callStatus = mapToCallStatus(callStatusResponse.getStatus());
            CallDirection callDirection = CallDirection.INBOUND;

            // 6. Search for existing lead (DO NOT CREATE)
            LeadSearchResponse leadInfo = findExistingLead(normalizedCallFrom);
            if (leadInfo == null) {
                log.info("No lead found for mobile: {} - skipping campaign call log", normalizedCallFrom);
                return;
            }

            UUID leadIdentifier = leadInfo.getLeadIdentifier();
            UUID contactPersonIdentifier = leadInfo.getContactPersonIdentifier();
            Long numberOfCampaignCalls = leadInfo.getNumberOfCampaignCalls() != null
                    ? leadInfo.getNumberOfCampaignCalls()
                    : 0L;

            log.info("Found lead: {} for mobile: {}", leadIdentifier, normalizedCallFrom);

            // 7. Resolve campaign UUID
            UUID campaignUUID = resolveCampaignId(campaignId);

            // 8. Build call log request
            CreateExternalCallLogRequest callLogRequest = buildCallLogRequest(
                    callStatusResponse,
                    contactPersonIdentifier,
                    callStatus,
                    callDirection,
                    normalizedCallFrom,
                    campaignUUID
            );

            // 9. Log the call
            leadCallWriteService.createExternalCallLog(leadIdentifier, callLogRequest);
            log.info("Created external call log for lead: {}, CallSid: {}", leadIdentifier, callSid);

            // 10. Update campaign call count
            if (campaignUUID != null) {
                updateCampaignCallCount(leadIdentifier, numberOfCampaignCalls + 1);
                log.info("Updated campaign call count for lead: {}", leadIdentifier);
            }

            log.info("Successfully processed campaign call status for CallSid: {}", callSid);

        } catch (Exception e) {
            log.error("Error processing campaign call status for CallSid: {}", callSid, e);
        }
    }

    /**
     * Resolve campaign ID - can be UUID or provider ID.
     */
    private UUID resolveCampaignId(String campaignId) {
        try {
            return UUID.fromString(campaignId);
        } catch (IllegalArgumentException e) {
            try {
                CampaignDetailedResponse campaign = campaignReadService.getCampaignByProviderId(campaignId);
                if (campaign != null && campaign.getIdentifier() != null) {
                    return UUID.fromString(campaign.getIdentifier());
                }
            } catch (Exception ex) {
                log.warn("Failed to resolve campaign provider ID: {}", campaignId, ex);
            }
            return null;
        }
    }

    /**
     * Extract the caller phone number from the call status response.
     * Prefers from.contactUri, falls back to call details.
     */
    private String extractCallFrom(VoiceGetCallStatusResponse response) {
        // Try from leg first
        if (response.getFrom() != null && response.getFrom().getContactUri() != null) {
            return response.getFrom().getContactUri();
        }

        // Fall back to call details
        if (response.getCallDetails() != null) {
            // Try virtualNumber or digits from call details
            if (response.getCallDetails().getVirtualNumber() != null) {
                return response.getCallDetails().getVirtualNumber();
            }
            if (response.getCallDetails().getDigits() != null) {
                return response.getCallDetails().getDigits();
            }
        }

        return null;
    }

    /**
     * Normalize phone number by removing country code prefix if present.
     * Converts +919876543210 to 9876543210.
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return phoneNumber;
        }

        // First normalize to standard format with country code
        String normalized = PhoneNumberUtils.normalizePhoneNumber(phoneNumber);
        
        // Remove +91 prefix for India to get 10-digit number
        if (normalized != null && normalized.startsWith("+91") && normalized.length() == 13) {
            return normalized.substring(3); // Remove "+91"
        }
        
        // Remove just the + if present
        if (normalized != null && normalized.startsWith("+")) {
            String digits = normalized.substring(1);
            // If it starts with 91 and is 12 digits, remove the 91
            if (digits.startsWith("91") && digits.length() == 12) {
                return digits.substring(2);
            }
            return digits;
        }

        return normalized;
    }

    /**
     * Map VoiceStatus to CallStatus.
     */
    private CallStatus mapToCallStatus(com.nivasafinance.services.voice.dto.VoiceStatus voiceStatus) {
        if (voiceStatus == null) {
            return CallStatus.FAILED;
        }
        return CallStatus.fromVoiceStatus(voiceStatus);
    }

    /**
     * Map direction string to CallDirection enum.
     */
    private CallDirection mapToCallDirection(String direction) {
        if (direction == null) {
            return CallDirection.INBOUND;
        }

        return switch (direction.toLowerCase()) {
            case "outbound", "outbound-api" -> CallDirection.OUTBOUND;
            case "inbound" -> CallDirection.INBOUND;
            default -> CallDirection.INBOUND;
        };
    }

    /**
     * Find an existing lead by mobile number, or create a new one if not found.
     */
    private LeadSearchResponse findOrCreateLead(String mobileNumber) {
        // Search for existing lead
        LeadSearchResponse existingLead = findExistingLead(mobileNumber);
        
        // If no lead found, create a new one
        if (existingLead == null) {
            log.info("No existing lead found for mobile: {}, creating new lead", mobileNumber);
            existingLead = createNewLead(mobileNumber);
        }

        return existingLead;
    }

    /**
     * Find an existing lead by mobile number. Does NOT create a new lead if not found.
     * Returns null if no lead exists.
     */
    private LeadSearchResponse findExistingLead(String mobileNumber) {
        // Search for existing lead
        PaginationRequest paginationRequest = new PaginationRequest(0, 10, "leadCreatedAt", "DESC");
        LeadSearchRequest searchRequest = new LeadSearchRequest(mobileNumber);

        PaginatedResponse<LeadSearchResponse> searchResult = leadReadService.searchLeads(
                paginationRequest,
                searchRequest
        );

        LeadSearchResponse selectedLead = null;

        if (searchResult != null && searchResult.getContent() != null && !searchResult.getContent().isEmpty()) {
            List<LeadSearchResponse> leads = searchResult.getContent();

            // Prefer ACTIVE leads (most recent first by leadCreatedAt)
            selectedLead = leads.stream()
                    .filter(lead -> lead.getStatus() == LeadStatus.ACTIVE)
                    .max(Comparator.comparing(LeadSearchResponse::getLeadCreatedAt))
                    .orElse(null);

            // If no active lead, get the most recent lead of any status
            if (selectedLead == null) {
                selectedLead = leads.stream()
                        .max(Comparator.comparing(LeadSearchResponse::getLeadCreatedAt))
                        .orElse(null);
            }
        }

        return selectedLead;
    }

    /**
     * Create a new lead with the given mobile number.
     */
    private LeadSearchResponse createNewLead(String mobileNumber) {
        CreateLeadRequest createRequest = new CreateLeadRequest();
        CreateLeadRequest.MobileNumberDetails phoneNumberDetails = 
                new CreateLeadRequest.MobileNumberDetails(mobileNumber, false);
        createRequest.setPhoneNumber(phoneNumberDetails);

        CreateLeadResponse createResponse = leadWriteService.createLead(createRequest);
        UUID leadIdentifier = createResponse.getLeadIdentifier();

        // Update sourcing details to mark as direct call source
        UpdateSourcingDetailsRequest sourcingRequest = new UpdateSourcingDetailsRequest();
        sourcingRequest.setSourcingChannel(DIRECT_CALL_SOURCE);
        leadWriteService.updateSourcingDetails(leadIdentifier, sourcingRequest);

        log.info("Created new lead: {} with sourcing channel: {}", leadIdentifier, DIRECT_CALL_SOURCE);

        // Fetch the created lead to return full details
        PaginationRequest paginationRequest = new PaginationRequest(0, 1, "leadCreatedAt", "DESC");
        LeadSearchRequest searchRequest = new LeadSearchRequest(mobileNumber);
        PaginatedResponse<LeadSearchResponse> searchResult = leadReadService.searchLeads(
                paginationRequest,
                searchRequest
        );

        if (searchResult != null && searchResult.getContent() != null && !searchResult.getContent().isEmpty()) {
            return searchResult.getContent().get(0);
        }

        // Fallback: return minimal info
        return LeadSearchResponse.builder()
                .leadIdentifier(leadIdentifier)
                .contactPersonIdentifier(createResponse.getContactIdentifier())
                .numberOfCampaignCalls(0L)
                .build();
    }

    /**
     * Extract campaign ID from call details if present.
     * Returns UUID if campaignId is a valid UUID string.
     */
    private UUID extractCampaignId(VoiceGetCallStatusResponse response) {
        if (response.getCallDetails() == null || response.getCallDetails().getCampaignId() == null) {
            return null;
        }

        String campaignIdStr = response.getCallDetails().getCampaignId();
        return resolveCampaignId(campaignIdStr);
    }

    /**
     * Extract campaign ID from customField JSON when entityType is CAMPAIGN and identifier is present.
     * Used as fallback when extractCampaignId returns null.
     */
    private UUID extractCampaignIdFromCustomField(String customField) {
        if (customField == null || customField.isBlank()) {
            return null;
        }
        String trimmed = customField.trim();
        if ("N/A".equalsIgnoreCase(trimmed)) {
            return null;
        }
        try {
            String decoded = trimmed;
            if (decoded.startsWith("\"") && decoded.endsWith("\"") && decoded.length() >= 2) {
                decoded = decoded.substring(1, decoded.length() - 1);
            }
            decoded = decoded.replace("\\\"", "\"");
            if (decoded.isBlank() || "null".equalsIgnoreCase(decoded)) {
                return null;
            }
            JsonNode node = objectMapper.readTree(decoded);
            if (node.isTextual()) {
                String inner = node.asText();
                if (inner != null && !inner.isBlank()) {
                    node = objectMapper.readTree(inner);
                }
            }
            if (!node.isObject()) {
                return null;
            }
            String entityType = node.path("entityType").asText(null);
            String identifier = node.path("identifier").asText(null);
            if (!ENTITY_TYPE_CAMPAIGN.equals(entityType) || identifier == null || identifier.isBlank()) {
                return null;
            }
            UUID campaignId = resolveCampaignId(identifier);
            if (campaignId != null) {
                log.info("Setting campaignId from CustomField: {}", identifier);
            }
            return campaignId;
        } catch (Exception e) {
            log.warn("Failed to parse customField for campaignId: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Build the CreateExternalCallLogRequest from call status response.
     */
    private CreateExternalCallLogRequest buildCallLogRequest(
            VoiceGetCallStatusResponse response,
            UUID contactIdentifier,
            CallStatus status,
            CallDirection direction,
            String fromNumber,
            UUID campaignId
    ) {
        VoiceGetCallStatusResponse.CallDetails callDetails = response.getCallDetails();
        String providerId = response.getCallId();
        String callerId = callDetails != null && callDetails.getVirtualNumber() != null
                ? callDetails.getVirtualNumber()
                : "EXOTEL";
        String toNumber = extractToNumber(response);

        // Extract recording URL from first recording if available
        CallLog.RecordingDetails recordingDetails = null;
        if (callDetails != null && callDetails.getRecordings() != null && !callDetails.getRecordings().isEmpty()) {
            String recordingUrl = callDetails.getRecordings().get(0).getUrl();
            if (recordingUrl != null && !recordingUrl.isBlank()) {
                recordingDetails = CallLog.RecordingDetails.builder()
                        .url(recordingUrl)
                        .build();
            }
        }

        // Build completion details if available
        CallLog.CompletionDetails completionDetails = null;
        if (callDetails != null) {
            if (callDetails.getTotalDuration() != null || callDetails.getStartTime() != null || callDetails.getEndTime() != null) {
                // Build leg details from from/to legs
                List<CallLog.CompletionLeg> legs = buildCompletionLegs(response);
                
                completionDetails = CallLog.CompletionDetails.builder()
                        .duration(callDetails.getTotalDuration() != null ? callDetails.getTotalDuration().longValue() : null)
                        .startTime(callDetails.getStartTime())
                        .endTime(callDetails.getEndTime())
                        .legs(legs)
                        .build();
            }
        }

        return CreateExternalCallLogRequest.builder()
                .contactIdentifier(contactIdentifier)
                .provider(CallProvider.EXOTEL)
                .providerId(providerId)
                .callerId(callerId)
                .fromNumber(fromNumber)
                .toNumber(toNumber)
                .direction(direction)
                .status(status)
                .createdAt(callDetails != null ? callDetails.getCreatedTime() : null)
                .campaignId(campaignId)
                .recordingDetails(recordingDetails)
                .completionDetails(completionDetails)
                .build();
    }

    /**
     * Build completion leg details from the from/to legs in the response.
     */
    private List<CallLog.CompletionLeg> buildCompletionLegs(VoiceGetCallStatusResponse response) {
        List<CallLog.CompletionLeg> legs = new java.util.ArrayList<>();
        
        // Add from leg if present
        if (response.getFrom() != null && response.getFrom().getStatus() != null) {
            CallLog.CompletionLeg fromLeg = CallLog.CompletionLeg.builder()
                    .status(mapToCallStatus(response.getFrom().getStatus()))
                    .duration(null) // Duration per leg not available in Exotel response
                    .direction("from")
                    .build();
            legs.add(fromLeg);
        }
        
        // Add to leg if present
        if (response.getTo() != null && response.getTo().getStatus() != null) {
            CallLog.CompletionLeg toLeg = CallLog.CompletionLeg.builder()
                    .status(mapToCallStatus(response.getTo().getStatus()))
                    .direction("to")
                    .duration(null) // Duration per leg not available in Exotel response
                    .build();
            legs.add(toLeg);
        }
        
        return legs.isEmpty() ? null : legs;
    }

    /**
     * Extract the "to" number from the call status response.
     */
    private String extractToNumber(VoiceGetCallStatusResponse response) {
        // Try to leg first
        if (response.getTo() != null && response.getTo().getContactUri() != null) {
            return response.getTo().getContactUri();
        }

        // Fall back to call details if available
        if (response.getCallDetails() != null && response.getCallDetails().getDigits() != null) {
            return response.getCallDetails().getDigits();
        }

        return null;
    }

    /**
     * Update the campaign call count for the lead.
     */
    private void updateCampaignCallCount(UUID leadIdentifier, Long newCount) {
        UpdateCallDetailsRequest request = UpdateCallDetailsRequest.builder()
                .noOfCampaignCalls(newCount)
                .build();

        leadWriteService.updateCallDetails(leadIdentifier, request);
    }

    @Async
    @Override
    public void processOutgoingCallback(String callId) {
        log.info("Processing outgoing callback for callId: {} with user: {}", callId, UserContext.getUsername());

        try {
            // 1. Validate callId
            if (callId == null || callId.isBlank()) {
                log.error("callId is null or blank");
                throw new IllegalArgumentException("callId cannot be null or blank");
            }

            // 2. Fetch Exotel call data
            VoiceHandler voiceHandler = voiceServiceFactory.getHandler(ThirdPartyServiceList.VOICE);
            BusinessContext businessContext = new BusinessContext(
                    SystemEntities.LEAD.name(),
                    null,
                    "EXOTEL_OUTGOING_CALLBACK"
            );

            VoiceGetCallStatusResponse callStatusResponse = voiceHandler.getCallStatus(callId, businessContext);
            log.info("Fetched call status for callId: {}", callId);

            // 3. Extract leadId and advisorId from call details
            String leadId = null;
            String advisorId = null;
            if (callStatusResponse.getCallDetails() != null) {
                leadId = callStatusResponse.getCallDetails().getLeadId();
                advisorId = callStatusResponse.getCallDetails().getAdvisorId();
            }

            log.info("Extracted leadId: {}, advisorId: {} from callId: {}", leadId, advisorId, callId);

            // 4. Determine which entity to update based on leadId or advisorId
            if (leadId != null && !leadId.isBlank()) {
                // Update lead call log
                updateLeadCallLog(leadId, callId, callStatusResponse);
            } else if (advisorId != null && !advisorId.isBlank()) {
                // Update advisor call log
                updateAdvisorCallLog(advisorId, callId, callStatusResponse);
            } else {
                log.warn("Neither leadId nor advisorId found in call details for callId: {}", callId);
            }

            log.info("Successfully processed outgoing callback for callId: {}", callId);

        } catch (Exception e) {
            log.error("Error processing outgoing callback for callId: {}", callId, e);
            // Don't rethrow - async method should handle exceptions gracefully
        }
    }

    /**
     * Update call log for a lead.
     */
    private void updateLeadCallLog(String leadId, String callId, VoiceGetCallStatusResponse callStatusResponse) {
        try {
            UUID leadIdentifier = UUID.fromString(leadId);
            log.info("Updating lead call log - leadId: {}, callId: {}", leadIdentifier, callId);

            // Map call status
            CallStatus callStatus = mapToCallStatus(callStatusResponse.getStatus());

            // Build LeadUpdateCallLog
            LeadUpdateCallLog updateCallLog = buildLeadUpdateCallLog(callStatusResponse, callStatus);

            // Update call log
            leadCallWriteService.updateCallLog(leadIdentifier, callId, updateCallLog);
            log.info("Successfully updated lead call log - leadId: {}, callId: {}", leadIdentifier, callId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid leadId format: {}", leadId, e);
        } catch (Exception e) {
            log.error("Error updating lead call log - leadId: {}, callId: {}", leadId, callId, e);
        }
    }

    /**
     * Update call log for an advisor.
     */
    private void updateAdvisorCallLog(String advisorId, String callId, VoiceGetCallStatusResponse callStatusResponse) {
        try {
            UUID advisorIdentifier = UUID.fromString(advisorId);
            log.info("Updating advisor call log - advisorId: {}, callId: {}", advisorIdentifier, callId);

            // Map call status
            CallStatus callStatus = mapToCallStatus(callStatusResponse.getStatus());

            // Build AdvisorUpdateCallLog
            AdvisorUpdateCallLog updateCallLog = buildAdvisorUpdateCallLog(callStatusResponse, callStatus);

            // Update call log
            advisorCallWriteService.updateCallLog(advisorIdentifier, callId, updateCallLog);
            log.info("Successfully updated advisor call log - advisorId: {}, callId: {}", advisorIdentifier, callId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid advisorId format: {}", advisorId, e);
        } catch (Exception e) {
            log.error("Error updating advisor call log - advisorId: {}, callId: {}", advisorId, callId, e);
        }
    }

    /**
     * Build LeadUpdateCallLog from call status response.
     */
    private LeadUpdateCallLog buildLeadUpdateCallLog(VoiceGetCallStatusResponse response, CallStatus status) {
        LeadUpdateCallLog.LeadUpdateCallLogBuilder builder = LeadUpdateCallLog.builder();
        builder.status(status);

        // Build recording details if available
        if (response.getCallDetails() != null &&
            response.getCallDetails().getRecordings() != null &&
            !response.getCallDetails().getRecordings().isEmpty()) {
            String recordingUrl = response.getCallDetails().getRecordings().get(0).getUrl();
            if (recordingUrl != null && !recordingUrl.isBlank()) {
                builder.recordingDetails(LeadUpdateCallLog.RecordingDetails.builder()
                        .url(recordingUrl)
                        .build());
            }
        }

        // Build completion details if available
        if (response.getCallDetails() != null) {
            VoiceGetCallStatusResponse.CallDetails callDetails = response.getCallDetails();
            LeadUpdateCallLog.CompletionDetails.CompletionDetailsBuilder completionBuilder =
                    LeadUpdateCallLog.CompletionDetails.builder();

            completionBuilder
                    .duration(callDetails.getTotalDuration() != null ? callDetails.getTotalDuration().longValue() : null)
                    .startTime(callDetails.getStartTime())
                    .endTime(callDetails.getEndTime());

            // Build legs
            List<LeadUpdateCallLog.CompletionLeg> legs = buildLeadCompletionLegs(response);
            if (legs != null && !legs.isEmpty()) {
                completionBuilder.legs(legs);
            }

            builder.completionDetails(completionBuilder.build());
        }

        return builder.build();
    }

    /**
     * Build AdvisorUpdateCallLog from call status response.
     */
    private AdvisorUpdateCallLog buildAdvisorUpdateCallLog(VoiceGetCallStatusResponse response, CallStatus status) {
        AdvisorUpdateCallLog.AdvisorUpdateCallLogBuilder builder = AdvisorUpdateCallLog.builder();
        builder.status(status);

        // Build recording details if available
        if (response.getCallDetails() != null &&
            response.getCallDetails().getRecordings() != null &&
            !response.getCallDetails().getRecordings().isEmpty()) {
            String recordingUrl = response.getCallDetails().getRecordings().get(0).getUrl();
            if (recordingUrl != null && !recordingUrl.isBlank()) {
                builder.recordingDetails(AdvisorUpdateCallLog.RecordingDetails.builder()
                        .url(recordingUrl)
                        .build());
            }
        }

        // Build completion details if available
        if (response.getCallDetails() != null) {
            VoiceGetCallStatusResponse.CallDetails callDetails = response.getCallDetails();
            AdvisorUpdateCallLog.CompletionDetails.CompletionDetailsBuilder completionBuilder =
                    AdvisorUpdateCallLog.CompletionDetails.builder();

            completionBuilder
                    .duration(callDetails.getTotalDuration() != null ? callDetails.getTotalDuration().longValue() : null)
                    .startTime(callDetails.getStartTime())
                    .endTime(callDetails.getEndTime());

            // Build legs
            List<AdvisorUpdateCallLog.CompletionLeg> legs = buildAdvisorCompletionLegs(response);
            if (legs != null && !legs.isEmpty()) {
                completionBuilder.legs(legs);
            }

            builder.completionDetails(completionBuilder.build());
        }

        return builder.build();
    }

    /**
     * Build completion legs for lead update call log.
     */
    private List<LeadUpdateCallLog.CompletionLeg> buildLeadCompletionLegs(VoiceGetCallStatusResponse response) {
        List<LeadUpdateCallLog.CompletionLeg> legs = new ArrayList<>();

        // Add from leg if present
        if (response.getFrom() != null && response.getFrom().getStatus() != null) {
            LeadUpdateCallLog.CompletionLeg fromLeg = LeadUpdateCallLog.CompletionLeg.builder()
                    .status(mapToCallStatus(response.getFrom().getStatus()))
                    .direction("from")
                    .duration(null) // Duration per leg not available
                    .build();
            legs.add(fromLeg);
        }

        // Add to leg if present
        if (response.getTo() != null && response.getTo().getStatus() != null) {
            LeadUpdateCallLog.CompletionLeg toLeg = LeadUpdateCallLog.CompletionLeg.builder()
                    .status(mapToCallStatus(response.getTo().getStatus()))
                    .direction("to")
                    .duration(null) // Duration per leg not available
                    .build();
            legs.add(toLeg);
        }

        return legs.isEmpty() ? null : legs;
    }

    /**
     * Build completion legs for advisor update call log.
     */
    private List<AdvisorUpdateCallLog.CompletionLeg> buildAdvisorCompletionLegs(VoiceGetCallStatusResponse response) {
        List<AdvisorUpdateCallLog.CompletionLeg> legs = new ArrayList<>();

        // Add from leg if present
        if (response.getFrom() != null && response.getFrom().getStatus() != null) {
            AdvisorUpdateCallLog.CompletionLeg fromLeg = AdvisorUpdateCallLog.CompletionLeg.builder()
                    .status(mapToCallStatus(response.getFrom().getStatus()))
                    .direction("from")
                    .duration(null) // Duration per leg not available
                    .build();
            legs.add(fromLeg);
        }

        // Add to leg if present
        if (response.getTo() != null && response.getTo().getStatus() != null) {
            AdvisorUpdateCallLog.CompletionLeg toLeg = AdvisorUpdateCallLog.CompletionLeg.builder()
                    .status(mapToCallStatus(response.getTo().getStatus()))
                    .direction("to")
                    .duration(null) // Duration per leg not available
                    .build();
            legs.add(toLeg);
        }

        return legs.isEmpty() ? null : legs;
    }

    @Async
    @Override
    public void processCampaignCallback(String campaignSid) {
        log.info("Processing campaign callback for campaign_sid: {} with user: {}", campaignSid, UserContext.getUsername());

        try {
            // 1. Validate campaignSid
            if (campaignSid == null || campaignSid.isBlank()) {
                log.error("campaign_sid is null or blank");
                throw new IllegalArgumentException("campaign_sid cannot be null or blank");
            }

            // 2. Get campaign details using provider ID
            log.info("Fetching campaign by provider ID: {}", campaignSid);
            CampaignDetailedResponse campaign = campaignReadService.getCampaignByProviderId(campaignSid);
            
            if (campaign == null || campaign.getIdentifier() == null) {
                log.error("Campaign not found for provider ID: {}", campaignSid);
                return;
            }

            UUID campaignIdentifier = UUID.fromString(campaign.getIdentifier());
            log.info("Found campaign - name: {}, identifier: {}", campaign.getName(), campaignIdentifier);

            // 3. Trigger campaign refresh
            log.info("Triggering campaign refresh for identifier: {}", campaignIdentifier);
            campaignWriteService.refreshCampaign(campaignIdentifier);
            log.info("Successfully triggered campaign refresh for identifier: {}", campaignIdentifier);

        } catch (IllegalArgumentException e) {
            log.error("Invalid campaign_sid or identifier format: {}", campaignSid, e);
        } catch (Exception e) {
            log.error("Error processing campaign callback for campaign_sid: {}", campaignSid, e);
            // Don't rethrow - async method should handle exceptions gracefully
        }
    }

    @Async
    @Override
    public void processAdvisorMissedCall(String callSid) {
        log.info("Processing advisor missed call for CallSid: {} with user: {}", callSid, UserContext.getUsername());

        try {
            // 1. Validate callSid
            if (callSid == null || callSid.isBlank()) {
                log.error("CallSid is null or blank");
                throw new IllegalArgumentException("CallSid cannot be null or blank");
            }

            // 2. Fetch Exotel call data
            VoiceHandler voiceHandler = voiceServiceFactory.getHandler(ThirdPartyServiceList.VOICE);
            BusinessContext businessContext = new BusinessContext(
                    SystemEntities.ADVISOR.name(),
                    null,
                    "EXOTEL_ADVISOR_MISSED_CALL"
            );

            VoiceGetCallStatusResponse callStatusResponse = voiceHandler.getCallStatus(callSid, businessContext);
            log.info("Fetched call status for CallSid: {}", callSid);

            // 3. Extract call_from (caller phone number)
            String callFrom = extractCallFrom(callStatusResponse);
            if (callFrom == null || callFrom.isBlank()) {
                log.error("Could not extract call_from for CallSid: {}", callSid);
                return;
            }

            // Normalize phone number (remove country code, etc.)
            String normalizedCallFrom = normalizePhoneNumber(callFrom);
            log.info("Normalized call_from: {} -> {}", callFrom, normalizedCallFrom);

            // 4. Map Exotel status and direction to CRM enums
            CallStatus callStatus = mapToCallStatus(callStatusResponse.getStatus());
            CallDirection callDirection = CallDirection.INBOUND;

            // 5. Find advisor by mobile number
            AdvisorBasicResponse advisorInfo = findOrCreateAdvisor(normalizedCallFrom);
            if (advisorInfo == null) {
                log.error("Failed to find or create advisor for mobile: {}", normalizedCallFrom);
                return;
            }

            UUID advisorIdentifier = advisorInfo.getAdvisorIdentifier();
            log.info("Found/Created advisor: {} for mobile: {}", advisorIdentifier, normalizedCallFrom);

            // 6. Build CreateExternalCallLogRequest for advisor
            com.nivasafinance.features.advisor.dto.CreateExternalCallLogRequest callLogRequest = 
                    buildAdvisorCallLogRequest(
                            callStatusResponse,
                            callStatus,
                            callDirection,
                            normalizedCallFrom
                    );

            // 7. Log the call
            advisorCallWriteService.createExternalCallLog(advisorIdentifier, callLogRequest);
            log.info("Created external call log for advisor: {}, CallSid: {}", advisorIdentifier, callSid);

            log.info("Successfully processed advisor missed call for CallSid: {}", callSid);

        } catch (Exception e) {
            log.error("Error processing advisor missed call for CallSid: {}", callSid, e);
            // Don't rethrow - async method should handle exceptions gracefully
        }
    }

    @Async
    @Override
    public void processAdvisorAnsweredCall(String callSid) {
        log.info("Processing advisor answered call for CallSid: {} with user: {}", callSid, UserContext.getUsername());

        try {
            // 1. Validate callSid
            if (callSid == null || callSid.isBlank()) {
                log.error("CallSid is null or blank");
                throw new IllegalArgumentException("CallSid cannot be null or blank");
            }

            // 2. Fetch Exotel call data
            VoiceHandler voiceHandler = voiceServiceFactory.getHandler(ThirdPartyServiceList.VOICE);
            BusinessContext businessContext = new BusinessContext(
                    SystemEntities.ADVISOR.name(),
                    null,
                    "EXOTEL_ADVISOR_ANSWERED_CALL"
            );

            VoiceGetCallStatusResponse callStatusResponse = voiceHandler.getCallStatus(callSid, businessContext);
            log.info("Fetched call status for CallSid: {}", callSid);

            // 3. Extract call_from (caller phone number)
            String callFrom = extractCallFrom(callStatusResponse);
            if (callFrom == null || callFrom.isBlank()) {
                log.error("Could not extract call_from for CallSid: {}", callSid);
                return;
            }

            // Normalize phone number (remove country code, etc.)
            String normalizedCallFrom = normalizePhoneNumber(callFrom);
            log.info("Normalized call_from: {} -> {}", callFrom, normalizedCallFrom);

            // 4. Map Exotel status and direction to CRM enums
            CallStatus callStatus = mapToCallStatus(callStatusResponse.getStatus());
            CallDirection callDirection = CallDirection.INBOUND;

            // 5. Search for existing advisor by mobile number (DO NOT CREATE)
            AdvisorBasicResponse advisorInfo = findExistingAdvisor(normalizedCallFrom);
            if (advisorInfo == null) {
                log.info("No advisor found for mobile: {} - skipping call logging for answered call", normalizedCallFrom);
                return;
            }

            UUID advisorIdentifier = advisorInfo.getAdvisorIdentifier();
            log.info("Found advisor: {} for mobile: {}", advisorIdentifier, normalizedCallFrom);

            // 6. Build CreateExternalCallLogRequest for advisor
            com.nivasafinance.features.advisor.dto.CreateExternalCallLogRequest callLogRequest = 
                    buildAdvisorCallLogRequest(
                            callStatusResponse,
                            callStatus,
                            callDirection,
                            normalizedCallFrom
                    );

            // 7. Log the call
            advisorCallWriteService.createExternalCallLog(advisorIdentifier, callLogRequest);
            log.info("Created external call log for advisor: {}, CallSid: {}", advisorIdentifier, callSid);

            log.info("Successfully processed advisor answered call for CallSid: {}", callSid);

        } catch (Exception e) {
            log.error("Error processing advisor answered call for CallSid: {}", callSid, e);
            // Don't rethrow - async method should handle exceptions gracefully
        }
    }

    /**
     * Find an existing advisor by mobile number, or create a new one if not found.
     */
    private AdvisorBasicResponse findOrCreateAdvisor(String mobileNumber) {
        // Search for existing advisor
        AdvisorBasicResponse existingAdvisor = findExistingAdvisor(mobileNumber);
        
        // If no advisor found, create a new one
        if (existingAdvisor == null) {
            log.info("No existing advisor found for mobile: {}, creating new advisor", mobileNumber);
            existingAdvisor = createNewAdvisor(mobileNumber);
        }

        return existingAdvisor;
    }

    /**
     * Find an existing advisor by mobile number. Does NOT create a new advisor if not found.
     * Returns null if no advisor exists.
     */
    private AdvisorBasicResponse findExistingAdvisor(String mobileNumber) {
        // Search for existing advisor
        PaginationRequest paginationRequest = new PaginationRequest(0, 10, "createdAt", "DESC");
        AdvisorSearchRequest searchRequest = new AdvisorSearchRequest(mobileNumber);

        PaginatedResponse<AdvisorBasicResponse> searchResult = advisorReadService.searchAdvisors(
                paginationRequest,
                searchRequest
        );

        if (searchResult != null && searchResult.getContent() != null && !searchResult.getContent().isEmpty()) {
            List<AdvisorBasicResponse> advisors = searchResult.getContent();
            // Return the most recent advisor
            return advisors.stream()
                    .max(Comparator.comparing(AdvisorBasicResponse::getCreatedAt))
                    .orElse(null);
        }

        return null;
    }

    /**
     * Create a new advisor with the given mobile number.
     */
    private AdvisorBasicResponse createNewAdvisor(String mobileNumber) {
        CreateAdvisorRequest createRequest = new CreateAdvisorRequest();
        MobileNumberDetails mobileNumberDetails = new MobileNumberDetails(mobileNumber, true, false);
        createRequest.setMobileNumberDetails(mobileNumberDetails);

        UUID advisorIdentifier = advisorWriteService.createAdvisor(createRequest);

        // Update sourcing details to mark as direct call source
        com.nivasafinance.features.advisor.dto.UpdateSourcingDetailsRequest sourcingRequest = 
                new com.nivasafinance.features.advisor.dto.UpdateSourcingDetailsRequest();
        sourcingRequest.setSourcingChannel(DIRECT_CALL_SOURCE);
        advisorWriteService.updateSourcingDetails(advisorIdentifier, sourcingRequest);

        log.info("Created new advisor: {} with sourcing channel: {}", advisorIdentifier, DIRECT_CALL_SOURCE);

        // Fetch the created advisor to return full details
        PaginationRequest paginationRequest = new PaginationRequest(0, 1, "createdAt", "DESC");
        AdvisorSearchRequest searchRequest = new AdvisorSearchRequest(mobileNumber);
        PaginatedResponse<AdvisorBasicResponse> searchResult = advisorReadService.searchAdvisors(
                paginationRequest,
                searchRequest
        );

        if (searchResult != null && searchResult.getContent() != null && !searchResult.getContent().isEmpty()) {
            return searchResult.getContent().get(0);
        }

        // Fallback: return minimal info
        return AdvisorBasicResponse.builder()
                .advisorIdentifier(advisorIdentifier)
                .mobileNumber(mobileNumber)
                .build();
    }

    /**
     * Build the CreateExternalCallLogRequest for advisor from call status response.
     */
    private com.nivasafinance.features.advisor.dto.CreateExternalCallLogRequest buildAdvisorCallLogRequest(
            VoiceGetCallStatusResponse response,
            CallStatus status,
            CallDirection direction,
            String fromNumber
    ) {
        VoiceGetCallStatusResponse.CallDetails callDetails = response.getCallDetails();
        String providerId = response.getCallId();
        String callerId = callDetails != null && callDetails.getVirtualNumber() != null
                ? callDetails.getVirtualNumber()
                : "EXOTEL";
        String toNumber = extractToNumber(response);

        // Extract recording URL from first recording if available
        CallLog.RecordingDetails recordingDetails = null;
        if (callDetails != null && callDetails.getRecordings() != null && !callDetails.getRecordings().isEmpty()) {
            String recordingUrl = callDetails.getRecordings().get(0).getUrl();
            if (recordingUrl != null && !recordingUrl.isBlank()) {
                recordingDetails = CallLog.RecordingDetails.builder()
                        .url(recordingUrl)
                        .build();
            }
        }

        // Build completion details if available
        CallLog.CompletionDetails completionDetails = null;
        if (callDetails != null) {
            if (callDetails.getTotalDuration() != null || callDetails.getStartTime() != null || callDetails.getEndTime() != null) {
                // Build leg details from from/to legs
                List<CallLog.CompletionLeg> legs = buildCompletionLegs(response);
                
                completionDetails = CallLog.CompletionDetails.builder()
                        .duration(callDetails.getTotalDuration() != null ? callDetails.getTotalDuration().longValue() : null)
                        .startTime(callDetails.getStartTime())
                        .endTime(callDetails.getEndTime())
                        .legs(legs)
                        .build();
            }
        }

        return com.nivasafinance.features.advisor.dto.CreateExternalCallLogRequest.builder()
                .provider(CallProvider.EXOTEL)
                .providerId(providerId)
                .callerId(callerId)
                .fromNumber(fromNumber)
                .toNumber(toNumber)
                .direction(direction)
                .status(status)
                .createdAt(callDetails != null ? callDetails.getCreatedTime() : null)
                .recordingDetails(recordingDetails)
                .completionDetails(completionDetails)
                .build();
    }
}
