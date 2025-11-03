package com.nivasafinance.features.offices.service;

import com.nivasafinance.features.offices.dto.OfficeResponse;

import java.util.UUID;

public interface OfficeReadService {
    OfficeResponse getOffice(UUID id);
}

