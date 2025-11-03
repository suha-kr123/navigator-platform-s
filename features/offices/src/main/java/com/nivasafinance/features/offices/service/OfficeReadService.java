package com.nivasafinance.features.offices.service;

import com.nivasafinance.features.offices.dto.OfficeResponse;

public interface OfficeReadService {
    OfficeResponse getOfficeByKey(String key);
}

