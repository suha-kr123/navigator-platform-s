package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorAddressReadService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdvisorAddressReadServiceImpl implements AdvisorAddressReadService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final ObjectMapper objectMapper;

    private static final String ADDRESSES_KEY = "addresses";

    @Override
    public List<AddressData> getAddresses(UUID advisorIdentifier) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        Person person = personRepositoryWrapper.findByIdWithException(advisor.getPersonId());
        List<AddressData> addresses = extractAddresses(person);
        if (addresses == null) {
            return Collections.emptyList();
        }
        return List.copyOf(addresses);
    }

    @Override
    public AddressData getAddress(UUID advisorIdentifier, String addressId) {
        return getAddresses(advisorIdentifier).stream()
                .filter(address -> addressId.equals(address.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Address not found for advisor"));
    }

    private List<AddressData> extractAddresses(Person person) {
        Map<String, Object> extData = person.getExtData();
        if (extData == null || extData.get(ADDRESSES_KEY) == null) {
            return null;
        }
        return objectMapper.convertValue(extData.get(ADDRESSES_KEY), new TypeReference<List<AddressData>>() {});
    }
}


