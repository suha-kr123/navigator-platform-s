package com.nivasafinance.features.offices.service;

import com.nivasafinance.features.offices.dto.OfficeCreateRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;

public interface OfficeWriteService {
    OfficeResponse createOffice(OfficeCreateRequest request);

    OfficeResponse moveOffice(String officeKey, String newParentKey);

    OfficeResponse activateOffice(String officeKey);

    OfficeResponse deactivateOfficeCascade(String officeKey);
}
