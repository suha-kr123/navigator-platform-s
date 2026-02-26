package com.nivasafinance.features.lender.lender.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.dto.LenderSearchRequest;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;

import java.util.List;
import java.util.UUID;

public interface LenderReadService {
    LenderResponseData getById(UUID id);
    LenderResponseData getByKey(String key);
    List<LenderResponseData> getAllByStatus(LenderStatus status);
    PaginatedResponse<LenderResponseData> getLendersPaginated(PaginationRequest paginationRequest, LenderStatus status);
    PaginatedResponse<LenderResponseData> searchLenders(PaginationRequest paginationRequest, LenderSearchRequest request);
}

