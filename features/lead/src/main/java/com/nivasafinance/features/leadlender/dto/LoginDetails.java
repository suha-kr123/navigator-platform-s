package com.nivasafinance.features.leadlender.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate loginDate;
    
    private BigDecimal loginFees;
}

