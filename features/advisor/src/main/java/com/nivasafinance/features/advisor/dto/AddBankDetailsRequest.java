package com.nivasafinance.features.advisor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddBankDetailsRequest {
    
    private Boolean isPrimary;
    
    @NotBlank(message = "Name as per passbook is required")
    private String nameAsPerPassbook;
    
    @NotBlank(message = "Account number is required")
    private String accountNo;
    
    @NotBlank(message = "IFSC code is required")
    private String ifscCode;
    
    private String bankName;
    
    private List<UpidDetails> upid;
}

