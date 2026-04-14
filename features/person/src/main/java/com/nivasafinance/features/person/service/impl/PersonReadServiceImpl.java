package com.nivasafinance.features.person.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.person.dto.AdminPersonResponse;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    public Optional<PersonResponse> findPersonByPrimaryMobile(String mobileNumber) {
        return personRepositoryWrapper.findByPrimaryMobileNumber(mobileNumber).map(this::mapToResponse);
    }

    @Override
    public Optional<PersonResponse> findPersonByEmail(String email) {
        return personRepositoryWrapper.findPersonByEmail(email).map(this::mapToResponse);
    }

    private PersonResponse mapToResponse(Person person) {
        return PersonResponse.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .middleName(person.getMiddleName())
                .lastName(person.getLastName())
                .displayName(person.getDisplayName())
                .email(person.getEmail())
                .mobileNumbers(person.getMobileNumbers())
                .dateOfBirth(person.getDateOfBirth())
                .gender(person.getGender())
                .extData(person.getExtData())
                .cbEnquiryId(person.getCbEnquiryId())
                .cbDetails(person.getCbDetails())
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

    @Override
    public List<PersonResponse> getPersonByMobile(String mobileNumber) {
        List<Person> persons = personRepositoryWrapper.findByMobileNumberWithException(mobileNumber);
        return persons.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<AdminPersonResponse> adminSearchPersonsByMobile(PaginationRequest paginationRequest, String mobileNumber) {
        return personRepositoryWrapper.adminSearchPersonsByMobileNumber(paginationRequest, mobileNumber);
    }

    @Override
    public PaginatedResponse<AdminPersonResponse> getDeletedPersons(PaginationRequest paginationRequest) {
        return personRepositoryWrapper.findDeletedPersons(paginationRequest);
    }


}

