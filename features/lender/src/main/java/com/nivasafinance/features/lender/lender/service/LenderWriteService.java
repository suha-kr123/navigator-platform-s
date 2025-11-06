package com.nivasafinance.features.lender.lender.service;

import com.nivasafinance.features.lender.lender.dto.LenderRequestData;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;

import java.util.UUID;

public interface LenderWriteService {
    LenderResponseData create(LenderRequestData lenderData);
    LenderResponseData update(UUID id, LenderRequestData lenderData);
    void delete(UUID id);
}

