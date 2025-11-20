package com.nivasafinance.features.person.service;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.person.dto.PersonResponse;

import java.util.List;

public interface PersonReadService {
    PersonResponse getPersonById(Long personId);
    PersonResponse getPersonByPrimaryMobile(String mobileNumber);
    List<AddressData> getAddresses(Long personId);
    AddressData getAddress(Long personId, String addressId);
}

