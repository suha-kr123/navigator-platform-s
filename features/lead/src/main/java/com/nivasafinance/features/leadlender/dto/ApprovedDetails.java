package com.nivasafinance.features.leadlender.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate approvedDate;
    
    private BigDecimal processingFees;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sanctionExpiry;
    
    private BigDecimal insuranceFees;
}

