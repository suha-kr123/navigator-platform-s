package com.nivasafinance.features.advisor.dto;

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
public class AdvisorLeadResponse {
    Long id;
    UUID leadIdentifier;
    String primaryContactName;
    String primaryContactPhone;
    BigDecimal requestedAmount;
    String currentStage;
    String status;
    String substatus;
    LocalDateTime createdAt;
    String office;
}
