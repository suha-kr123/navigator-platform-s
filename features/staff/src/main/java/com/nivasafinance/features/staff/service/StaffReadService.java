package com.nivasafinance.features.staff.service;

import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.entity.Staff;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffReadService {

    PaginatedResponse<StaffResponse> getStaff(String officeKey, String nameQuery, PaginationRequest paginationRequest);
    StaffResponse getCurrentStaff();
    List<StaffResponse> getStaffByOfficeKeys(List<String> officeKeys);
    Optional<StaffResponse> getStaffByIdentifier(UUID identifier);

    Optional<Staff> findStaffByUserIdIncludingDeleted(Long userId);

    Staff findStaffByIdentifierIncludingDeleted(UUID identifier);

    PaginatedResponse<StaffResponse> getDeletedStaff(PaginationRequest paginationRequest);

    PaginatedResponse<StaffResponse> adminSearchStaff(String name, PaginationRequest paginationRequest);
}