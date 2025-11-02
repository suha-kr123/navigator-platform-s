package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.enums.TenureType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDisbursementDetailsRequest {
    
    @NotNull(message = "Disbursed amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Disbursed amount must be greater than 0")
    private BigDecimal disbursedAmount;
    
    @DecimalMin(value = "0.0", message = "ROI must be non-negative")
    private BigDecimal roi;
    
    @Min(value = 1, message = "Tenure value must be at least 1")
    private Integer tenureValue;
    
    private TenureType tenureType;
    
    private LocalDate disbursedDate;
    
    private BigDecimal processingFees;
}

