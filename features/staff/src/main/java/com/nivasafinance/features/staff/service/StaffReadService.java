package com.nivasafinance.features.staff.service;

import com.nivasafinance.features.staff.dto.StaffResponse;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import java.util.List;

public interface StaffReadService {

    PaginatedResponse<StaffResponse> getStaff(String officeKey, String nameQuery, PaginationRequest paginationRequest);
}