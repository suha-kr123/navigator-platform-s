package com.nivasafinance.features.usermanagement.service;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import com.nivasafinance.features.usermanagement.dto.UserResponse;

public interface UserWriteService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse createUserForExistingPerson(UserCreateRequest request, Long personId);

    UserResponse createUserForMobile(String mobile, PersonCreateRequest personDetailsForCreate);
//
//    void updatePersonForUser(Long userId, PersonUpdateRequest request);

    void updatePersonForUser(String username, PersonUpdateRequest request);

//    String addAddressForUser(Long userId, AddressRequest request);

    String addAddressForUser(String username, AddressRequest request);

//    AddressData updateAddressForUser(Long userId, String addressId, AddressRequest request);

    AddressData updateAddressForUser(String username, String addressId, AddressRequest request);
}


