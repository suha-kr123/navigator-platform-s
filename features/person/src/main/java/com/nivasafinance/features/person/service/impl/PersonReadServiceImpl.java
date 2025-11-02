package com.nivasafinance.features.person.service.impl;

import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}

