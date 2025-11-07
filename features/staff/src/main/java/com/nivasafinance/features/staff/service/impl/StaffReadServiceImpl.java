package com.nivasafinance.features.staff.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.repository.StaffRepositoryWrapper;
import com.nivasafinance.features.staff.service.StaffReadService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.repository.UserRepositoryWrapper;
import com.nivasafinance.features.offices.service.OfficeReadService;
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
    private final UserRepositoryWrapper userRepositoryWrapper;
    private final PersonReadService personReadService;
    private final OfficeReadService officeReadService;

    @Override
    public PaginatedResponse<StaffResponse> getStaff(String officeKey, String nameQuery, PaginationRequest paginationRequest) {
        if (StringUtils.hasText(officeKey)) {
            officeReadService.getOfficeByKey(officeKey);
        }

        PaginatedResponse<Staff> paginatedStaff = staffRepositoryWrapper.findStaff(officeKey, nameQuery, paginationRequest);

        List<StaffResponse> content = paginatedStaff.getContent().stream()
                .map(staff -> mapToResponse(staff, userRepositoryWrapper.findByIdWithException(staff.getUserId())))
                .collect(Collectors.toList());

        return new PaginatedResponse<>(content, paginatedStaff.getPagination());
    }

    private StaffResponse mapToResponse(Staff staff, User user) {
        PersonResponse personResponse = null;
        if (user.getPerson() != null) {
            personResponse = personReadService.getPersonById(user.getPerson().getId());
        }

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .personResponse(personResponse)
                .username(user.getUsername())
                .status(user.getStatus())
                .build();

        return StaffResponse.builder()
                .id(staff.getId())
                .identifier(staff.getIdentifier())
                .officeKey(staff.getOfficeKey())
                .userResponse(userResponse)
                .build();
    }
}
