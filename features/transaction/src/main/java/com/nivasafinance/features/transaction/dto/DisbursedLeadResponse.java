package com.nivasafinance.features.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisbursedLeadResponse {

    private UUID leadIdentifier;
    private String applicantName;
    private String productCode;
    private BigDecimal disbursedAmount;
    private LocalDate disbursedDate;
    private String officeKey;
    private String referralCode;
    private String payeeType;
    private String payeeName;
    private UUID payeeIdentifier;
    private LocalDateTime createdAt;
}
