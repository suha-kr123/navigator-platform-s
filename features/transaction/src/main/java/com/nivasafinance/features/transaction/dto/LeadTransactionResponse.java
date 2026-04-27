package com.nivasafinance.features.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadTransactionResponse {

    private UUID identifier;
    private String status;
    private BigDecimal amount;
    private String createdBy;
    private LocalDateTime createdAt;

    private String domainType;
    private UUID leadIdentifier;
    private String referralCode;

    private String payeeType;
    private UUID payeeIdentifier;

    private String latestEventType;
    private String latestActor;
    private LocalDateTime latestEventAt;
    private String remarks;

    private String paymentMode;
    private String externalReference;
    private String paymentStatus;
    private Map<String, Object> paymentData;
}
