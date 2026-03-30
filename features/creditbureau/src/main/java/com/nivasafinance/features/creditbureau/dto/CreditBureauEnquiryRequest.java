package com.nivasafinance.features.creditbureau.dto;

import com.nivasafinance.common.dto.AddressData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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
    private List<AddressData> addressesForCreditBureauPull;
}
