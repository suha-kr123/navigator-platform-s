package com.nivasafinance.features.leadqueues.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.nivasafinance.features.lead.dto.LeadResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadQueueResponse {

    private String queueConfigName;
    private UUID leadIdentifier;
    private LeadResponse lead;
    private LocalDateTime calculatedAt;
    private Integer position;
    private String currentlyClaimedBy;
    private LocalDateTime claimExpiryAt;
    private List<ClaimHistory> claimedHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClaimHistory {
        private LocalDateTime claimedAt;
        private String claimedBy;
        private String unclaimedBy;
        private LocalDateTime unclaimedAt;
    }

}
