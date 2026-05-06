package com.nivasafinance.features.advisor.dto.self;

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
public class SelfPayoutDetailResponse {
    private UUID transactionIdentifier;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
    private AdvisorSelfLeadResponse leadDetails;
    private SelfPaymentDetails paymentDetails;
    private String remarks;
}
