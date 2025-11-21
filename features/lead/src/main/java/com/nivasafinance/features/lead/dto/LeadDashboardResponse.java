package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private String advisorName;
    private String advisorNumber;
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
    private String office;
    private CodeValueResponse sourcingChannel;


    //TODO :: to add stage, substage, next task data, stageOwner, stageTat
}
