package com.nivasafinance.features.staff.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.repository.StaffRepositoryWrapper;
import com.nivasafinance.features.staff.service.StaffReadService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StaffReadServiceImpl implements StaffReadService {

    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final UserReadService userReadService;
    private final OfficeReadService officeReadService;

    @Override
    public PaginatedResponse<StaffResponse> getStaff(String officeKey, String nameQuery, PaginationRequest paginationRequest) {
        if (StringUtils.hasText(officeKey)) {
            officeReadService.getOfficeByKey(officeKey);
        }

        PaginatedResponse<Staff> paginatedStaff = staffRepositoryWrapper.findStaff(officeKey, nameQuery, paginationRequest);

        List<StaffResponse> content = paginatedStaff.getContent().stream()
                .map(staff -> mapToResponse(staff, userReadService.getUserById(staff.getUserId())))
                .collect(Collectors.toList());

        return new PaginatedResponse<>(content, paginatedStaff.getPagination());
    }

    @Override
    public StaffResponse getCurrentStaff() {
        String currentUsername = UserContext.getUsername();

        if (!StringUtils.hasText(currentUsername)) {
            throw new IllegalStateException("No current user");
        }

        UserResponse user = userReadService.getUserByUsername(currentUsername);
        Staff staff = staffRepositoryWrapper.findByUserIdWithException(user.getId());
        return mapToResponse(staff, user);
    }

    @Override
    public List<StaffResponse> getStaffByOfficeKeys(List<String> officeKeys) {
        if (officeKeys == null || officeKeys.isEmpty()) {
            return List.of();
        }

        List<Staff> staffList = staffRepositoryWrapper.findAllByOfficeKeys(officeKeys);
        return staffList.stream()
                .map(staff -> mapToResponse(staff, userReadService.getUserById(staff.getUserId())))
                .collect(Collectors.toList());
    }

    private StaffResponse mapToResponse(Staff staff, UserResponse userResponse) {
        return StaffResponse.builder()
                .id(staff.getId())
                .identifier(staff.getIdentifier())
                .officeKey(staff.getOfficeKey())
                .officeName(officeReadService.getOfficeByKey(staff.getOfficeKey()).getName())
                .userResponse(userResponse)
                .build();
    }
}
