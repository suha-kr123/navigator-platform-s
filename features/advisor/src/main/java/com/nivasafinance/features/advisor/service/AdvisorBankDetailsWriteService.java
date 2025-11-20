package com.nivasafinance.features.advisor.service;

import com.nivasafinance.features.advisor.dto.AddBankDetailsRequest;
import com.nivasafinance.features.advisor.dto.*;

import java.util.UUID;

public interface AdvisorBankDetailsWriteService {
    
    UUID addBankDetails(UUID advisorIdentifier, AddBankDetailsRequest request);

    void updateBankDetails(UUID advisorIdentifier, UUID bankIdentifier, UpdateBankDetailsRequest request);

    UUID activateBankDetails(UUID advisorIdentifier, UUID bankIdentifier);

    UUID deactivateBankDetails(UUID advisorIdentifier, UUID bankIdentifier);
}

