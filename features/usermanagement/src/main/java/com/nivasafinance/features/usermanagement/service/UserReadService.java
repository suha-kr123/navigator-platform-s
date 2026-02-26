package com.nivasafinance.features.usermanagement.service;

import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.base.model.PaginatedResponse;

import java.util.List;
import java.util.Optional;

public interface UserReadService {

    UserResponse getUserById(Long userId);

    UserResponse getUserByUsername(String username);

    void checkForUserNameAvailability(String username);

    Optional<User> findUserByUsername(String username);

    List<User> findUsersByPersonPhoneNumber(String phoneNumber);
    
    PaginatedResponse<UserResponse> getUsers(PaginationRequest pagination, String q);
}
