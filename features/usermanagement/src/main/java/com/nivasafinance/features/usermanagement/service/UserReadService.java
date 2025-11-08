package com.nivasafinance.features.usermanagement.service;

import com.nivasafinance.features.usermanagement.dto.UserResponse;

public interface UserReadService {

    UserResponse getUserById(Long userId);

    UserResponse getUserByUsername(String username);

    void checkForUserNameAvailability(String username);
}

