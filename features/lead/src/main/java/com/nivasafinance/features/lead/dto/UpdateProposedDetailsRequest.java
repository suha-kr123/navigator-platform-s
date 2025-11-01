package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.enums.TenureType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProposedDetailsRequest {
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Proposed loan amount must be greater than 0")
    private BigDecimal proposedLoanAmount;
    
    @DecimalMin(value = "0.0", message = "ROI must be non-negative")
    private BigDecimal roi;
    
    @Min(value = 1, message = "Tenure value must be at least 1")
    private Integer tenureValue;
    
    private TenureType tenureType;
    
    @DecimalMin(value = "0.0", message = "EMI must be non-negative")
    private BigDecimal emi;
}

