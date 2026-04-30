package com.nivasafinance.features.advisor.dto.self;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorSelfLeadResponse {
    private UUID leadIdentifier;
    private String leadName;
    private String leadNumber;
    private String loanType;
    private LeadStatus leadStatus;
    private LeadSubStatus leadSubStatus;
    private String reasonCode;
    private String reason;
    private BigDecimal requestedAmount;
    private LocalDateTime createdAt;
    private String leadStageDisplayName;
    private BigDecimal disbursedAmount;
    private LocalDate disbursedDate;
}
