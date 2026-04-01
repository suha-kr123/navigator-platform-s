package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchLeadRequest {

    private Optional<PatchPropertyDetailsRequest> propertyDetails;
    private Optional<PatchIncomeAndObligationRequest> incomeAndObligationDetails;
    private Optional<String> currentCustomerFormStep;
    private Optional<String> productCode;
    private Optional<BigDecimal> requestedAmount;
}
