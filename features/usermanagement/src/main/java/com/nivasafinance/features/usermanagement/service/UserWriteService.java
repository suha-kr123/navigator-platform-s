package com.nivasafinance.features.usermanagement.service;

import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import com.nivasafinance.features.usermanagement.dto.UserResponse;

public interface UserWriteService {

    UserResponse createUser(UserCreateRequest request);
}


