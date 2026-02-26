package com.nivasafinance.features.bre.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.bre.dto.BREConfigDetailedResponse;
import com.nivasafinance.features.bre.dto.BREConfigResponse;

public interface BREConfigReadService {
    PaginatedResponse<BREConfigResponse> getAllBREConfigs(PaginationRequest paginationRequest);

    BREConfigDetailedResponse getBREConfigByUname(String uname);
}
