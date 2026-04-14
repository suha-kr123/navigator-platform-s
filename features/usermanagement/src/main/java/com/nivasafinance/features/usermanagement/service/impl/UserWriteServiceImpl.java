package com.nivasafinance.features.usermanagement.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.usermanagement.exception.UserExceptionFactory;
import com.nivasafinance.features.usermanagement.repository.UserRepository;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserWriteServiceImpl implements UserWriteService {

    private final UserRepository userRepository;
    private final PersonWriteService personWriteService;
    private final PersonReadService personReadService;
    private final UserReadService userReadService;
    private final MessageSource messageSource;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        if (request.getPerson() == null) {
            throw new BadRequestException("person is required for createUser");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw UserExceptionFactory.userAlreadyExists(request.getUsername());
        }

        PersonCreateResponse personCreateResponse = personWriteService.createPerson(request.getPerson());

        Person personReference = new Person();
        personReference.setId(personCreateResponse.getId());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE);
        user.setPerson(personReference);

        User savedUser = userRepository.save(user);

        PersonResponse personResponse = personReadService.getPersonById(personCreateResponse.getId());

        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .status(savedUser.getStatus())
                .personResponse(personResponse)
                .deleted(savedUser.getIsDeleted())
                .build();
    }

    @Override
    public UserResponse createUserForExistingPerson(UserCreateRequest request, Long personId) {
        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            throw new BadRequestException("username is required");
        }
        if (userRepository.existsByUsername(username)) {
            throw UserExceptionFactory.userAlreadyExists(username);
        }

        PersonResponse personResponse = personReadService.getPersonById(personId);

        Person personReference = new Person();
        personReference.setId(personId);

        User user = new User();
        user.setUsername(username);
        user.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE);
        user.setPerson(personReference);

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .status(savedUser.getStatus())
                .personResponse(personResponse)
                .deleted(savedUser.getIsDeleted())
                .build();
    }

    @Override
    public UserResponse createUserForMobile(String mobile, PersonCreateRequest personDetailsForCreate) {
        if (mobile == null || mobile.isBlank()) {
            throw new BadRequestException("Mobile number is required for createUserForMobile");
        }
        Long personId = personReadService.findPersonByPrimaryMobile(mobile)
                .map(PersonResponse::getId)
                .orElseGet(() -> personWriteService.createPerson(personDetailsForCreate).getId());
        UserCreateRequest userRequest = UserCreateRequest.builder().username(mobile).build();
        return createUserForExistingPerson(userRequest, personId);
    }

    @Override
    public UserResponse activateDeactivateUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserExceptionFactory.userNotFoundByUsername(username));
        user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        PersonResponse personResponse = null;
        if (savedUser.getPerson() != null) {
            personResponse = personReadService.getPersonById(savedUser.getPerson().getId());
        }
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .status(savedUser.getStatus())
                .personResponse(personResponse)
                .deleted(savedUser.getIsDeleted())
                .build();
    }

    @Override
    public void updatePersonForUser(String username, PersonUpdateRequest request) {
        Long personId = userReadService.getPersonIdByUsername(username);
        personWriteService.updatePerson(personId, request);
    }


    @Override
    public String addAddressForUser(String username, AddressRequest request) {
        Long personId = userReadService.getPersonIdByUsername(username);
        return personWriteService.addAddress(personId, request);
    }


    @Override
    public AddressData updateAddressForUser(String username, String addressId, AddressRequest request) {
        Long personId = userReadService.getPersonIdByUsername(username);
        return personWriteService.updateAddress(personId, addressId, request);
    }

    @Override
    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserExceptionFactory.userNotFoundByUsername(username));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw UserExceptionFactory.userAlreadyDeleted(username, messageSource);
        }

        user.setIsDeleted(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void undoDeleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserExceptionFactory.userNotFoundByUsername(username));

        if (!Boolean.TRUE.equals(user.getIsDeleted())) {
            throw UserExceptionFactory.userNotDeleted(username, messageSource);
        }

        user.setIsDeleted(false);
        userRepository.save(user);
    }
}
