package com.nivasafinance.features.lender.lenderoffice.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeSearchRequest;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;

import java.util.List;
import java.util.UUID;

public interface LenderOfficeReadService {
    LenderOfficeReponseData getByKey(String key);
    LenderOfficeReponseData getById(UUID id);
    List<LenderOfficeReponseData> getByLenderKeyAndStatus(String lenderKey, LenderOfficeStatus status);
    PaginatedResponse<LenderOfficeReponseData> getLenderOfficesPaginated(String lenderKey, PaginationRequest paginationRequest,
                                                                         LenderOfficeStatus status);
    PaginatedResponse<LenderOfficeReponseData> searchLenderOffices(String lenderKey, PaginationRequest paginationRequest,
                                                                   LenderOfficeSearchRequest request);
}

