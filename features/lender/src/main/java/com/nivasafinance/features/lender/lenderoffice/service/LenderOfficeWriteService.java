package com.nivasafinance.features.lender.lenderoffice.service;

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeRequestData;

import java.util.UUID;

public interface LenderOfficeWriteService {
    LenderOfficeReponseData create(LenderOfficeRequestData lenderOfficeData);
    LenderOfficeReponseData update(UUID id, LenderOfficeRequestData lenderOfficeData);
    void delete(UUID id);
}

