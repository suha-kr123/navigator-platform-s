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
public class LeadSearchResponse {
    private UUID leadIdentifier;
    private BigDecimal requestedAmount;
    private String productName;
    private UUID primaryPersonIdentifier;
    private String primaryPersonName;
    private String primaryPersonNumber;
    private UUID contactPersonIdentifier;
    private String contactPersonName;
    private String contactPersonNumber;
    private LeadStatus status;
    private LeadSubStatus subStatus;
    private LocalDateTime leadCreatedAt;
    private LocalDateTime lastActivityDate;
}
