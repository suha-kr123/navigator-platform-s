package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadResponse {
    private UUID leadIdentifier;
    private BigDecimal requestedAmount;
    private String productCode;
    private String productName;
    private String purpose;
    private String primaryPersonName;
    private String primaryPersonNumber;
    private String officeName;
    private String officeKey;
    private String currentStage;
    private String ownerUsername;
    private LeadStatus status;
    private LeadSubStatus subStatus;
    private String reasonCode;
    private String reason;

    public static LeadResponse leadToResponse(Lead lead) {
        LeadResponse.LeadResponseBuilder builder = LeadResponse.builder()
                .leadIdentifier(lead.getLeadIdentifier());
        return builder.build();
    }
}
