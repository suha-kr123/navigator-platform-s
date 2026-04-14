package com.nivasafinance.features.staff.service;

import com.nivasafinance.features.staff.dto.StaffCreateRequest;
import com.nivasafinance.features.staff.dto.StaffResponse;

import java.util.UUID;

public interface StaffWriteService {

    StaffResponse createStaff(StaffCreateRequest request);

    void mapUserToOffice(String username, String officeKey);

    void deleteStaff(UUID identifier);

    void undoDeleteStaff(UUID identifier);
}
