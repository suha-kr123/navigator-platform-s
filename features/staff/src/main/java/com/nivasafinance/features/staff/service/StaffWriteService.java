package com.nivasafinance.features.staff.service;

import com.nivasafinance.features.staff.dto.StaffCreateRequest;
import com.nivasafinance.features.staff.dto.StaffResponse;

public interface StaffWriteService {

    StaffResponse createStaff(StaffCreateRequest request);

    void mapUserToOffice(String username, String officeKey);
}
