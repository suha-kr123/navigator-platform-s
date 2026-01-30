package com.nivasafinance.features.staff.service.impl;

import com.nivasafinance.features.staff.dto.StaffCreateRequest;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.repository.StaffRepositoryWrapper;
import com.nivasafinance.features.staff.service.StaffWriteService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.staff.dto.StaffCreateRequest.Role;
import com.nivasafinance.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffWriteServiceImpl implements StaffWriteService {

    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final UserReadService userReadService;
    private final UserWriteService userWriteService;
    private final OfficeReadService officeReadService;
    private final UserRoleService userRoleService;
    @Override
    public StaffResponse createStaff(StaffCreateRequest request) {
        officeReadService.getOfficeByKey(request.getOfficeKey());

        UserCreateRequest userRequest = request.getUser();

        userReadService.checkForUserNameAvailability(userRequest.getUsername());

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .username(userRequest.getUsername())
                .status(userRequest.getStatus() != null ? userRequest.getStatus() : UserStatus.ACTIVE)
                .person(userRequest.getPerson())
                .build();

        UserResponse createdUser = userWriteService.createUser(userCreateRequest);

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            validateOnlyOnePrimaryRole(request.getRoles());
            for (Role role : request.getRoles()) {
                userRoleService.saveRolesForUsername(
                        createdUser.getUsername(),
                        role.getRolename(),
                        role.getIsPrimary()
                );
            }
        }

        Staff staff = new Staff();
        staff.setIdentifier(UUID.randomUUID());
        staff.setUserId(createdUser.getId());
        staff.setOfficeKey(request.getOfficeKey());

        Staff saved = staffRepositoryWrapper.saveWithException(staff);

        return mapToResponse(saved, createdUser);
    }

    private StaffResponse mapToResponse(Staff staff, UserResponse userResponse) {
        UserResponse resolvedUser = userResponse != null
                ? userResponse
                : userReadService.getUserById(staff.getUserId());

        return StaffResponse.builder()
                .id(staff.getId())
                .identifier(staff.getIdentifier())
                .officeKey(staff.getOfficeKey())
                .userResponse(resolvedUser)
                .build();
    }

    private void validateOnlyOnePrimaryRole(List<Role> roles) {
        long primaryRoleCount = roles.stream()
                .filter(role -> role.getIsPrimary() != null && role.getIsPrimary())
                .count();
        
        if (primaryRoleCount > 1) {
            throw new ValidationException("Only one role can be marked as primary");
        }
    }
}

