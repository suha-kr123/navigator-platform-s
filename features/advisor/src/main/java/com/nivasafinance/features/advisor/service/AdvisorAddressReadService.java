package com.nivasafinance.features.advisor.service;

import com.nivasafinance.common.dto.AddressData;

import java.util.List;
import java.util.UUID;

public interface AdvisorAddressReadService {

    List<AddressData> getAddresses(UUID advisorIdentifier);

    AddressData getAddress(UUID advisorIdentifier, String addressId);
}


