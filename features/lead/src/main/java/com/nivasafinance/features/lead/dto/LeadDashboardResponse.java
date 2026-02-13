package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.referral.enums.EntityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeadDashboardResponse {
    private UUID leadIdentifier;
    private BigDecimal requestedAmount;
    private BigDecimal eligibleAmount;
    private BigDecimal proposedAmount;
    private BigDecimal disbursedAmount;
    private String productName;
    private String primaryPersonName;
    private String primaryPersonNumber;
    private String officeName;
    private String ownerUsername;
    private LeadStatus status;
    private LeadSubStatus subStatus;
    private String recentNote;
    private LocalDateTime leadCreatedAt;
    private LocalDateTime lastActivityDate;
    private String lastActivityBy;
    private String leadOwner;
    private LocalTime preferredCallStartTime;
    private LocalTime preferredCallEndTime;
    private CodeValueResponse priority;
    private String partners;
    private Long numberOfCalls;
    private String lastCallDirection;
    private String lastCallStatus;
    private LocalDateTime lastCallDate;
    private CodeValueResponse onHoldReason;
    private LocalDateTime onHoldDate;
    private LocalDate holdFollowUpDate;
    private String office;
    private CodeValueResponse sourcingChannel;
    private Long noOfCampaignCalls;
    
    // Workflow details
    private String workflowConfigKey;
    private String currentStageKey;
    private String currentSubStageKey;
    private String currentSubStageName;
    private String stageAssignedTo;
    private LocalDateTime stageAssignedAt;
    private LocalDateTime stageEnteredAt;

    //referral details
    private String referredByCode;
    private UUID referredByIdentifier;
    private EntityType referredByType;
    private String referredByName;
    private String referredByNumber;
}
