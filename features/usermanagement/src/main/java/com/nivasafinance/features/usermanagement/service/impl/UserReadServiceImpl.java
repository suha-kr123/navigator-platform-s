package com.nivasafinance.features.usermanagement.service.impl;

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

    @Override
    public UserResponse getUserByUsername(String username) {
        User user = userRepositoryWrapper.findByUsernameWithException(username);
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .personId(user.getPerson() != null ? user.getPerson().getId() : null)
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
    }
}

