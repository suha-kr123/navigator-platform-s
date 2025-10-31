package com.nivasafinance.features.leadlender.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDetails {
    
    private String loginId;
    
    private LocalDate loginDate;
    
    private BigDecimal loginFees;
}

