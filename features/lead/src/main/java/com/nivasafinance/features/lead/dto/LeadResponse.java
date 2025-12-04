package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadResponse {
    private UUID leadIdentifier;
    private BigDecimal requestedAmount;
    private BigDecimal eligibleAmount;
    private BigDecimal disbursedAmount;
    private String productCode;
    private String productName;
    private String purpose;
    private String primaryPersonName;
    private String primaryPersonNumber;
    private String officeName;
    private String officeKey;
    private String ownerUsername;
    private LeadStatus status;
    private LeadSubStatus subStatus;
    private String reasonCode;
    private String reason;
    private LocalDate leadCreatedAt;
    private LocalDate holdFollowUpDate;
    private LocalTime preferredCallStartTime;
    private LocalTime preferredCallEndTime;
    private CodeValueResponse priority;
    private BigDecimal proposedAmount;
    private BigDecimal proposedRoi;
    private CodeValueResponse bureauRating;
    private CodeValueResponse customerProfiles;
    private CodeValueResponse monthlyFamilyIncome;
    private BigDecimal eligibleLoanAmount;
    private String advisorIdentifier;
    private String advisorName;
    private String advisorNumber;
    private String recentNote;
    private String noteCreatedBy;
    private LocalDateTime noteCreatedAt;
    private String partnerIdentifier;
    private String partnerName;
    private String partnerStatus;
    private CodeValueResponse partnerStage;
    private String partnerOfficeName;
    private String lenderIdentifier;
    private String lenderName;
    private String lenderStatus;
    private CodeValueResponse lenderStage;
    private String lenderOfficeName;

    //workflow details
    private String workflowConfigKey;
    private String currentStageKey;
    private String currentSubStageKey;
    private String currentSubStageName;
    private String assignedTo;
    private LocalDateTime assignedAt;
    private LocalDateTime enteredAt;

}
