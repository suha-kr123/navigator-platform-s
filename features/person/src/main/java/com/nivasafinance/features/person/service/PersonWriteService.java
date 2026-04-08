package com.nivasafinance.features.person.service;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PersonWriteService {
    PersonCreateResponse createPerson(PersonCreateRequest personRequest);
    void updatePerson(Long personId, PersonUpdateRequest personUpdateRequest);
    String addAddress(Long personId, AddressRequest request);
    AddressData updateAddress(Long personId, String addressId, AddressRequest request);

    IdentifierData addIdentifier(Long personId, IdentifierRequest request);

    void updateIdentifier(Long personId, UUID identifierId, IdentifierRequest request);

    void deleteIdentifier(Long personId, UUID identifierId);

    void updateCreditBureauFields(Long personId, List<Long> cbEnquiryIds, Person.CreditBureauDetails cbDetails);

    void updateDateOfBirthIfAbsent(Long personId, LocalDate dateOfBirth);

    void deletePerson(String mobileNumber);

    void undoDeletePerson(String mobileNumber);
}

