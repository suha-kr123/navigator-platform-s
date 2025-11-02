package com.nivasafinance.features.lead.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLeadRequest {
    
    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Requested amount must be greater than 0")
    private BigDecimal requestedAmount;
    
    private String officeKey;
    
    private String owner;
    
    private String purpose;
    
    private String productCode;
}

