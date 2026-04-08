package com.nivasafinance.features.admin.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.admin.exception.AdminExceptionFactory;
import com.nivasafinance.features.admin.service.AdminStaffService;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.service.StaffReadService;
import com.nivasafinance.features.staff.service.StaffWriteService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminStaffServiceImpl implements AdminStaffService {

    private final StaffWriteService staffWriteService;
    private final StaffReadService staffReadService;
    private final UserReadService userReadService;
    private final MessageSource messageSource;

    @Override
    public void deleteStaff(UUID identifier) {
        staffWriteService.deleteStaff(identifier);
    }

    @Override
    public void undoDeleteStaff(UUID identifier) {
        Staff staff = staffReadService.findStaffByIdentifierIncludingDeleted(identifier);
        UserResponse user = userReadService.adminGetUserById(staff.getUserId());
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw AdminExceptionFactory.cannotRestoreStaffUserDeleted(messageSource);
        }
        staffWriteService.undoDeleteStaff(identifier);
    }

    @Override
    public PaginatedResponse<StaffResponse> getDeletedStaff(PaginationRequest paginationRequest) {
        return staffReadService.getDeletedStaff(paginationRequest);
    }

    @Override
    public PaginatedResponse<StaffResponse> adminSearchStaff(String name, PaginationRequest paginationRequest) {
        return staffReadService.adminSearchStaff(name, paginationRequest);
    }
}
