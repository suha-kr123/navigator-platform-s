package com.nivasafinance.features.lender.lenderoffice.service;

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeRequestData;
import com.nivasafinance.features.lender.lenderoffice.dto.UpdateLenderOfficeRequest;

import java.util.UUID;

public interface LenderOfficeWriteService {
    LenderOfficeReponseData create(String lenderKey, LenderOfficeRequestData lenderOfficeData);
    LenderOfficeReponseData update(UUID id, LenderOfficeRequestData lenderOfficeData);
    LenderOfficeReponseData updateOffice(UUID id, UpdateLenderOfficeRequest request);
    void delete(UUID id);
    LenderOfficeReponseData activateDeactivateLenderOffice(UUID officeId);
}

