package com.nivasafinance.features.advisor.service;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;

import java.util.UUID;

public interface AdvisorAddressWriteService {

    String addAddress(UUID advisorIdentifier, AddressRequest request);

    AddressData updateAddress(UUID advisorIdentifier, String addressId, AddressRequest request);

    void deleteAddress(UUID advisorIdentifier, String addressId);
}


