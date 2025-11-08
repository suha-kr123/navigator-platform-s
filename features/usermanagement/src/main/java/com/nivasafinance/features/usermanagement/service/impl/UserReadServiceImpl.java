package com.nivasafinance.features.usermanagement.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.repository.UserRepositoryWrapper;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserReadServiceImpl implements UserReadService {

    private final UserRepositoryWrapper userRepositoryWrapper;
    private final PersonReadService personReadService;

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepositoryWrapper.findByIdWithException(userId);
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        User user = userRepositoryWrapper.findByUsernameWithException(username);
        return mapToResponse(user);
    }

    @Override
    public void checkForUserNameAvailability(String username) {
        if(userRepositoryWrapper.existsByUsername(username)){
            throw new BadRequestException("Username is already taken");
        }
    }

    private UserResponse mapToResponse(User user) {
        // Fetch PersonResponse if person is linked
        PersonResponse personResponse = null;
        if (user.getPerson() != null) {
            personResponse = personReadService.getPersonById(user.getPerson().getId());
        }

        return UserResponse.builder()
                .id(user.getId())
                .personResponse(personResponse)  // Set full PersonResponse
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
    }
}