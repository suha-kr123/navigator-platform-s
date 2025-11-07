package com.nivasafinance.features.staff.service.impl;

import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.features.staff.dto.StaffCreateRequest;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.exception.StaffExceptionFactory;
import com.nivasafinance.features.staff.repository.StaffRepository;
import com.nivasafinance.features.staff.repository.StaffRepositoryWrapper;
import com.nivasafinance.features.staff.service.StaffWriteService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import com.nivasafinance.features.usermanagement.exception.UserExceptionFactory;
import com.nivasafinance.features.usermanagement.repository.UserRepositoryWrapper;
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
    private final UserRepositoryWrapper userRepositoryWrapper;
    private final PersonReadService personReadService;
    private final PersonWriteService personWriteService;
    private final OfficeReadService officeReadService;
    private final MessageSource messageSource;

    @Override
    public StaffResponse createStaff(StaffCreateRequest request) {
        officeReadService.getOfficeByKey(request.getOfficeKey());

        userRepositoryWrapper.findByUsername(request.getUsername()).ifPresent(existingUser -> {
            if (staffRepositoryWrapper.existsByUserIdAndOfficeKey(existingUser.getId(), request.getOfficeKey())) {
                throw StaffExceptionFactory.alreadyExists(existingUser.getId(), request.getOfficeKey(), messageSource);
            }
            throw UserExceptionFactory.userAlreadyExists(request.getUsername());
        });

        PersonCreateRequest personRequest = request.getPerson();
        PersonCreateResponse personResponse = personWriteService.createPerson(personRequest);

        Person personReference = new Person();
        personReference.setId(personResponse.getId());

        User user = new User();
        user.setPerson(personReference);
        user.setUsername(request.getUsername());
        user.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE);

        User savedUser = userRepositoryWrapper.save(user);

        Staff staff = new Staff();
        staff.setIdentifier(UUID.randomUUID());
        staff.setUserId(savedUser.getId());
        staff.setOfficeKey(request.getOfficeKey());

        Staff saved = staffRepository.save(staff);

        PersonResponse createdPerson = personReadService.getPersonById(personResponse.getId());

        return mapToResponse(saved, savedUser, createdPerson);
    }

    private StaffResponse mapToResponse(Staff staff, User user, PersonResponse personResponse) {
        PersonResponse resolvedPerson = personResponse;
        if (resolvedPerson == null && user.getPerson() != null) {
            resolvedPerson = personReadService.getPersonById(user.getPerson().getId());
        }

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .personResponse(resolvedPerson)
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

