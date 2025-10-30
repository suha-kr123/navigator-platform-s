package com.nivasafinance.features.leadlender.dto;

import com.nivasafinance.common.enums.TenureType;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadLenderRequest {
    
    private String lenderOfficeKey;
    private String rmName;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String rmMobileNumber;
    
    // Login details fields
    private String loginId;
    private LocalDate loginDate;
    private BigDecimal loginFees;
    
    // Approved details fields
    private BigDecimal approvedAmount;
    private BigDecimal roi;
    private Integer tenureValue;
    private TenureType tenureType;
    private LocalDate approvedDate;
    private BigDecimal processingFees;
    private LocalDate sanctionExpiry;
    private BigDecimal insuranceFees;
    
    // Stage
    private String stage;
    
    // Remarks
    private String remarks;
}
