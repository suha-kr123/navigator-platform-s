package com.nivasafinance.features.advisor.dto.self;

import com.nivasafinance.features.advisor.dto.UpidDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfAddBankDetailsRequest {
    private Optional<Boolean> isPrimary;
    private Optional<String> nameAsPerPassbook;
    private Optional<String> accountNo;
    private Optional<String> ifscCode;
    private Optional<String> bankName;
    private Optional<List<UpidDetails>> upid;
}
