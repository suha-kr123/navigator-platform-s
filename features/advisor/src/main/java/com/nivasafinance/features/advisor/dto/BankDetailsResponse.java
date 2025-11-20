package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.advisor.enums.BankDetailsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDetailsResponse {
    
    private UUID bankIdentifier;
    private Boolean isPrimary;
    private String nameAsPerPassbook;
    private String accountNo;
    private String bankName;
    private String ifscCode;
    private List<UpidDetails> upid;
    private BankDetailsStatus status;
}

