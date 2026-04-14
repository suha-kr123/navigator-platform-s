package com.nivasafinance.features.usermanagement.service;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserReadService {

    UserResponse getUserById(Long userId);

    UserResponse getUserByUsername(String username);

    void checkForUserNameAvailability(String username);

    Optional<User> findUserByUsername(String username);

    List<User> findUsersByPersonPhoneNumber(String phoneNumber);

    PaginatedResponse<UserResponse> getUsers(PaginationRequest pagination, String q);

    Optional<UserResponse> findUserByPersonMobile(String mobile);

    Optional<String> resolveUsernameByEmail(String email);

    Optional<String> resolveUsernameByPhone(String phone);

    Long getPersonIdByUsername(String username);

//    Long getPersonIdByUserId(Long userId);

//    PersonResponse getPersonForUser(Long userId);

    PersonResponse getPersonForUser(String username);

//    List<AddressData> getAddressesForUser(Long userId);

    List<AddressData> getAddressesForUser(String username);

//    AddressData getAddressForUser(Long userId, String addressId);

    AddressData getAddressForUser(String username, String addressId);

    UserResponse adminGetUserByUsername(String username);

    UserResponse adminGetUserById(Long userId);

    PaginatedResponse<UserResponse> getDeletedUsers(PaginationRequest pagination);
}

