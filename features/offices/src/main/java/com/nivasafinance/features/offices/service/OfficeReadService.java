package com.nivasafinance.features.offices.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.dto.OfficeTreeNodeResponse;

import java.util.List;

public interface OfficeReadService {
    OfficeResponse getOfficeByKey(String key);

    List<OfficeResponse> getOfficeByKeys(List<String> keys);

    PaginatedResponse<OfficeResponse> getOffices(String parentKey, String nameQuery, Boolean activeOnly, PaginationRequest paginationRequest);

    List<OfficeResponse> getOfficesByCodePrefix(String codePrefix);

    List<OfficeTreeNodeResponse> getOfficeTree(String parentKey, String search);
}
