package com.nivasafinance.features.person.service;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.person.dto.PersonResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonReadService {
    PersonResponse getPersonById(Long personId);
    PersonResponse getPersonByPrimaryMobile(String mobileNumber);
    Optional<PersonResponse> findPersonByPrimaryMobile(String mobileNumber);
    Optional<PersonResponse> findPersonByEmail(String email);
    List<PersonResponse> getPersonByMobile(String mobileNumber);
    List<AddressData> getAddresses(Long personId);
    AddressData getAddress(Long personId, String addressId);

    List<IdentifierData> getIdentifiers(Long personId);

    IdentifierData getIdentifier(Long personId, UUID identifierId);
}

