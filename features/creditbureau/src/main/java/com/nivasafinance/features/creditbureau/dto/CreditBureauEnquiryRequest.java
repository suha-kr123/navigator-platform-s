package com.nivasafinance.features.creditbureau.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditBureauEnquiryRequest {
    private Long personId;
    private String entityType;
    private Long entityId;
    private String businessPurpose;
    private UUID leadIdentifier;
    private UUID contactIdentifier;
}
