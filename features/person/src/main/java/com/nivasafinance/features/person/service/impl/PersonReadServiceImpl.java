package com.nivasafinance.features.person.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonReadService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PersonReadServiceImpl implements PersonReadService {

    private final PersonRepositoryWrapper personRepositoryWrapper;

    @Override
    public PersonResponse getPersonById(Long personId) {
        Person person = personRepositoryWrapper.findByIdWithException(personId);
        return mapToResponse(person);
    }

    @Override
    public PersonResponse getPersonByPrimaryMobile(String mobileNumber) {
        Person person = personRepositoryWrapper.findByPrimaryMobileNumberWithException(mobileNumber);
        return mapToResponse(person);
    }

    private PersonResponse mapToResponse(Person person) {
        return PersonResponse.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .middleName(person.getMiddleName())
                .lastName(person.getLastName())
                .displayName(person.getDisplayName())
                .mobileNumbers(person.getMobileNumbers())
                .dateOfBirth(person.getDateOfBirth())
                .gender(person.getGender())
                .extData(person.getExtData())
                .createdAt(person.getCreatedAt())
                .createdBy(person.getCreatedBy())
                .updatedAt(person.getUpdatedAt())
                .updatedBy(person.getUpdatedBy())
                .build();
    }

    @Override
    public List<AddressData> getAddresses(Long personId) {
        Person person = personRepositoryWrapper.findByIdWithException(personId);
        List<AddressData> addresses = person.getAddress();
        return addresses == null ? new ArrayList<>() : addresses;
    }

    @Override
    public AddressData getAddress(Long personId, String addressId) {
        List<AddressData> addresses = getAddresses(personId);
        return addresses.stream()
                .filter(address -> addressId.equals(address.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Address not found for person"));
    }

    @Override
    public List<IdentifierData> getIdentifiers(Long personId) {
        Person person = personRepositoryWrapper.findByIdWithException(personId);
        List<IdentifierData> identifiers = person.getIdentifiers();
        return identifiers == null ? new ArrayList<>() : identifiers;
    }

    @Override
    public IdentifierData getIdentifier(Long personId, UUID identifierId) {
        List<IdentifierData> identifiers = getIdentifiers(personId);
        return identifiers.stream()
                .filter(identifier -> identifierId.equals(identifier.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Identifier not found for person"));
    }
}

