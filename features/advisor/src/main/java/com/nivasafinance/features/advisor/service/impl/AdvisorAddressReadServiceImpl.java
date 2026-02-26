package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorAddressReadService;
import com.nivasafinance.features.usermanagement.service.UserReadService;
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
    private final UserReadService userReadService;

    @Override
    public List<AddressData> getAddresses(UUID advisorIdentifier) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        return userReadService.getAddressesForUser(advisor.getUsername());
    }

    @Override
    public AddressData getAddress(UUID advisorIdentifier, String addressId) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        try {
            return userReadService.getAddressForUser(advisor.getUsername(), addressId);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found for advisor");
        }
    }
}


