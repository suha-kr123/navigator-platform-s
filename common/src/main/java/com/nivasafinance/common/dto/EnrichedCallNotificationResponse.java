package com.nivasafinance.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedCallNotificationResponse {
    
    private String callSid; // providerId from CallLog
    private String callFrom; // fromNumber
    private String callTo; // toNumber
    private String callStatus; // status enum value
    private String direction; // direction enum value
    private String eventType; // can derive from status
    private LocalDateTime timestamp; // createdAt
    private LocalDateTime createdAt;
    
    // Enriched data - can have multiple leads/advisors for the same phone number
    private List<LeadInfo> leadInfos; // all leads linked to this call
    private List<AdvisorInfo> advisorInfos; // all advisors linked to this call
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeadInfo {
        private UUID leadIdentifier;
        private String leadStatus;
        private Long contactId;
        private String contactName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdvisorInfo {
        private UUID advisorIdentifier;
        private String advisorStatus;
        private String advisorName;
    }
}

