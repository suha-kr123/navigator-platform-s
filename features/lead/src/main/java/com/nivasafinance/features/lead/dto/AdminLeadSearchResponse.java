package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLeadSearchResponse {
    private UUID leadIdentifier;
    private BigDecimal requestedAmount;
    private String productName;
    private String primaryPersonName;
    private String primaryPersonNumber;
    private LeadStatus status;
    private LeadSubStatus subStatus;
    private LocalDateTime leadCreatedAt;
    private LocalDateTime lastActivityDate;
    private Boolean deleted;
}
