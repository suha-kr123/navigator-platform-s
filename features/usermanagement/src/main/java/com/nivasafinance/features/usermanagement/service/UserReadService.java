package com.nivasafinance.features.usermanagement.service;

import com.nivasafinance.features.usermanagement.dto.UserResponse;

public interface UserReadService {
    
    UserResponse getUserByUsername(String username);
}

