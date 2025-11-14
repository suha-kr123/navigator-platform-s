package com.nivasafinance.features.leadlender.dto;

import com.nivasafinance.common.enums.TenureType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovedDetails {
    
    private BigDecimal approvedAmount;
    
    private BigDecimal roi;
    
    private Integer tenureValue;
    
    private TenureType tenureType;
    
    private LocalDate approvedDate;
    
    private BigDecimal processingFees;
    
    private LocalDate sanctionExpiry;
    
    private BigDecimal insuranceFees;

    private BigDecimal otherFees;
}

