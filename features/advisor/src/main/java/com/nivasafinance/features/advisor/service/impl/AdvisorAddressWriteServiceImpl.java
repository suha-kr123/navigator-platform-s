package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorAddressWriteService;
import com.nivasafinance.features.person.service.PersonWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AdvisorAddressWriteServiceImpl implements AdvisorAddressWriteService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final PersonWriteService personWriteService;

    @Override
    public String addAddress(UUID advisorIdentifier, AddressRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        return personWriteService.addAddress(advisor.getPersonId(), request);
    }

    @Override
    public AddressData updateAddress(UUID advisorIdentifier, String addressId, AddressRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        return personWriteService.updateAddress(advisor.getPersonId(), addressId, request);
    }

}
