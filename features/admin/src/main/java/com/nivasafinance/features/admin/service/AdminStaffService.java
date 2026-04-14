package com.nivasafinance.features.admin.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.staff.dto.StaffResponse;

import java.util.UUID;

public interface AdminStaffService {
    void deleteStaff(UUID identifier);
    void undoDeleteStaff(UUID identifier);
    PaginatedResponse<StaffResponse> getDeletedStaff(PaginationRequest paginationRequest);
    PaginatedResponse<StaffResponse> adminSearchStaff(String name, PaginationRequest paginationRequest);
}
