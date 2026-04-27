package com.nivasafinance.features.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadTransactionContext {

    private UUID identifier;
    private Long leadId;
    private String domainType;
    private String referralCode;
}
