package com.nivasafinance.features.person.service;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;

public interface PersonWriteService {
    PersonCreateResponse createPerson(PersonCreateRequest personRequest);
    void updatePerson(Long personId, PersonUpdateRequest personUpdateRequest);
    String addAddress(Long personId, AddressRequest request);
    AddressData updateAddress(Long personId, String addressId, AddressRequest request);
}

