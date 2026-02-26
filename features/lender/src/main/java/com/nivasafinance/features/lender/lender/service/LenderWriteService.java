package com.nivasafinance.features.lender.lender.service;

import com.nivasafinance.features.lender.lender.dto.LenderRequestData;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.dto.UpdateLenderRequest;

import java.util.UUID;

public interface LenderWriteService {
    LenderResponseData create(LenderRequestData lenderData);
    LenderResponseData update(UUID id, UpdateLenderRequest request);
    void delete(UUID id);
    LenderResponseData activateDeactivateLender(UUID id);
}

