package com.nivasafinance.features.advisor.service;

import com.nivasafinance.features.advisor.dto.BankDetailsResponse;

import java.util.List;
import java.util.UUID;

public interface AdvisorBankDetailsReadService {
    
    List<BankDetailsResponse> getAllBankDetails(UUID advisorIdentifier);
}

