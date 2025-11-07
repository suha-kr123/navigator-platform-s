package com.nivasafinance.features.usermanagement.service.impl;

import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import com.nivasafinance.features.usermanagement.exception.UserExceptionFactory;
import com.nivasafinance.features.usermanagement.repository.UserRepository;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserWriteServiceImpl implements UserWriteService {

    private final UserRepository userRepository;
    private final PersonWriteService personWriteService;
    private final PersonReadService personReadService;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
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
                .build();
    }
}
