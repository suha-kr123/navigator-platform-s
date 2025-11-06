package com.nivasafinance.features.lender.lenderoffice.service;

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;

import java.util.List;
import java.util.UUID;

public interface LenderOfficeReadService {
    LenderOfficeReponseData getByKey(String key);
    LenderOfficeReponseData getById(UUID id);
    List<LenderOfficeReponseData> getByLenderKeyAndStatus(String lenderKey, LenderOfficeStatus status);
}

