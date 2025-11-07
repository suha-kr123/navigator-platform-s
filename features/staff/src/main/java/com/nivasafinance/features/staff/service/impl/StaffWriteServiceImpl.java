package com.nivasafinance.features.staff.service.impl;

import com.nivasafinance.features.staff.dto.StaffCreateRequest;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.exception.StaffExceptionFactory;
import com.nivasafinance.features.staff.repository.StaffRepository;
import com.nivasafinance.features.staff.repository.StaffRepositoryWrapper;
import com.nivasafinance.features.staff.service.StaffWriteService;
import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import com.nivasafinance.features.usermanagement.exception.UserExceptionFactory;
import com.nivasafinance.features.usermanagement.exception.UserNotFoundException;
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

    private final StaffRepository staffRepository;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final UserReadService userReadService;
    private final UserWriteService userWriteService;
    private final OfficeReadService officeReadService;
    private final MessageSource messageSource;

    @Override
    public StaffResponse createStaff(StaffCreateRequest request) {
        officeReadService.getOfficeByKey(request.getOfficeKey());

        UserResponse existingUser = null;
        try {
            existingUser = userReadService.getUserByUsername(request.getUsername());
        } catch (UserNotFoundException ignored) {
            // Username is available
        }

        if (existingUser != null) {
            if (staffRepositoryWrapper.existsByUserIdAndOfficeKey(existingUser.getId(), request.getOfficeKey())) {
                throw StaffExceptionFactory.alreadyExists(existingUser.getId(), request.getOfficeKey(), messageSource);
            }
            throw UserExceptionFactory.userAlreadyExists(request.getUsername());
        }

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .username(request.getUsername())
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .person(request.getPerson())
                .build();

        UserResponse createdUser = userWriteService.createUser(userCreateRequest);

        Staff staff = new Staff();
        staff.setIdentifier(UUID.randomUUID());
        staff.setUserId(createdUser.getId());
        staff.setOfficeKey(request.getOfficeKey());

        Staff saved = staffRepository.save(staff);

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

