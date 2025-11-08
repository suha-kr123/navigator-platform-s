package com.nivasafinance.features.offices.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;

public interface OfficeReadService {
    OfficeResponse getOfficeByKey(String key);

    PaginatedResponse<OfficeResponse> getOffices(String parentKey, String nameQuery, PaginationRequest paginationRequest);
}

