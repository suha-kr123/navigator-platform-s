package com.nivasafinance.features.staff.service.impl;

import com.nivasafinance.features.staff.dto.StaffCreateRequest;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.exception.StaffExceptionFactory;
import com.nivasafinance.features.staff.repository.StaffRepositoryWrapper;
import com.nivasafinance.features.staff.service.StaffWriteService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import com.nivasafinance.features.usermanagement.exception.UserExceptionFactory;
import com.nivasafinance.features.usermanagement.exception.UserNotFoundException;
import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import com.nivasafinance.features.offices.service.OfficeReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffWriteServiceImpl implements StaffWriteService {

    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final UserReadService userReadService;
    private final UserWriteService userWriteService;
    private final OfficeReadService officeReadService;
    private final MessageSource messageSource;

    @Override
    public StaffResponse createStaff(StaffCreateRequest request) {
        officeReadService.getOfficeByKey(request.getOfficeKey());

        UserCreateRequest userRequest = request.getUser();

        UserResponse existingUser = null;
        try {
            existingUser = userReadService.getUserByUsername(userRequest.getUsername());
        } catch (UserNotFoundException ignored) {
            // Username is available
        }

        if (existingUser != null) {
            if (staffRepositoryWrapper.existsByUserIdAndOfficeKey(existingUser.getId(), request.getOfficeKey())) {
                throw StaffExceptionFactory.alreadyExists(existingUser.getId(), request.getOfficeKey(), messageSource);
            }
            throw UserExceptionFactory.userAlreadyExists(userRequest.getUsername());
        }

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .username(userRequest.getUsername())
                .status(userRequest.getStatus() != null ? userRequest.getStatus() : UserStatus.ACTIVE)
                .person(userRequest.getPerson())
                .build();

        UserResponse createdUser = userWriteService.createUser(userCreateRequest);

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
}

