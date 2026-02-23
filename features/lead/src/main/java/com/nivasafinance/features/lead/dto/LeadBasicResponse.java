package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.referral.enums.EntityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeadBasicResponse {
    Long id;
    UUID leadIdentifier;
    String primaryContactName;
    String primaryContactPhone;
    BigDecimal requestedAmount;
    String currentStage;
    LeadStatus status;
    LeadSubStatus substatus;
    LocalDateTime createdAt;
    String office;
    String productCode;

    //referral details
    private String referredByCode;
    private UUID referredByIdentifier;
    private EntityType referredByType;
    private String referredByName;
    private String referredByNumber;
}
