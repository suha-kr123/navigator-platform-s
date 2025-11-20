package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorAddressReadService;
import com.nivasafinance.features.person.service.PersonReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdvisorAddressReadServiceImpl implements AdvisorAddressReadService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final PersonReadService personReadService;

    @Override
    public List<AddressData> getAddresses(UUID advisorIdentifier) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        return personReadService.getAddresses(advisor.getPersonId());
    }

    @Override
    public AddressData getAddress(UUID advisorIdentifier, String addressId) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        try {
            return personReadService.getAddress(advisor.getPersonId(), addressId);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found for advisor");
        }
    }
}


