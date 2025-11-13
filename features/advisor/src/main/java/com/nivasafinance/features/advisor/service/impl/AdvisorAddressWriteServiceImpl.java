package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorAddressWriteService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class AdvisorAddressWriteServiceImpl implements AdvisorAddressWriteService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final AddressDataService addressDataService;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final ObjectMapper objectMapper;

    private static final String ADDRESSES_KEY = "addresses";

    @Override
    public String addAddress(UUID advisorIdentifier, AddressRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        Person person = personRepositoryWrapper.findByIdWithException(advisor.getPersonId());
        List<AddressData> addresses = getAddresses(person);

        AddressData addressData = buildAddressData(request, null);
        addresses.add(addressData);

        saveAddresses(person, addresses);

        return addressData.getId();
    }

    @Override
    public AddressData updateAddress(UUID advisorIdentifier, String addressId, AddressRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        Person person = personRepositoryWrapper.findByIdWithException(advisor.getPersonId());
        List<AddressData> addresses = getAddresses(person);

        AddressData existing = addresses.stream()
                .filter(address -> addressId.equals(address.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Address not found for advisor"));

        AddressData updatedAddress = buildAddressData(request, existing.getId());

        int index = addresses.indexOf(existing);
        addresses.set(index, updatedAddress);

        saveAddresses(person, addresses);

        return updatedAddress;
    }

    @Override
    public void deleteAddress(UUID advisorIdentifier, String addressId) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        Person person = personRepositoryWrapper.findByIdWithException(advisor.getPersonId());
        List<AddressData> addresses = getAddresses(person);

        if (addresses.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found for advisor");
        }

        boolean removed = false;
        Iterator<AddressData> iterator = addresses.iterator();
        while (iterator.hasNext()) {
            AddressData address = iterator.next();
            if (addressId.equals(address.getId())) {
                iterator.remove();
                removed = true;
                break;
            }
        }

        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found for advisor");
        }

        saveAddresses(person, addresses);
    }

    private List<AddressData> getAddresses(Person person) {
        Map<String, Object> extData = person.getExtData();
        if (extData == null || extData.get(ADDRESSES_KEY) == null) {
            return new ArrayList<>();
        }
        Object value = extData.get(ADDRESSES_KEY);
        return objectMapper.convertValue(value, new TypeReference<List<AddressData>>() {});
    }

    private void saveAddresses(Person person, List<AddressData> addresses) {
        Map<String, Object> extData = person.getExtData();
        if (extData == null) {
            extData = new HashMap<>();
        }
        if (addresses == null || addresses.isEmpty()) {
            extData.remove(ADDRESSES_KEY);
        } else {
            List<AddressData> copy = new ArrayList<>(addresses);
            extData.put(ADDRESSES_KEY, copy);
        }
        if (extData.isEmpty()) {
            person.setExtData(null);
        } else {
            person.setExtData(extData);
        }
        personRepositoryWrapper.saveWithException(person);
    }

    private AddressData buildAddressData(AddressRequest request, String addressId) {
        AddressData addressData = addressDataService.createAddressData(request);
        addressData.setId(addressId != null ? addressId : UUID.randomUUID().toString());
        addressData.setAddressType(request.getAddressType());
        return addressData;
    }

}
