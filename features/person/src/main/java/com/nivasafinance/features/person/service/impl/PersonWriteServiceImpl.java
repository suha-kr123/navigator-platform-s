package com.nivasafinance.features.person.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.exception.PersonExceptionFactory;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonWriteService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class PersonWriteServiceImpl implements PersonWriteService {

    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final MessageSource messageSource;

    @Override
    public PersonCreateResponse createPerson(PersonCreateRequest request) {
        // Extract primary mobile number
        String primaryMobile = extractPrimaryMobileNumber(request.getMobileNumbers());
        
        // Check if primary mobile already exists
        if (primaryMobile != null) {
            Optional<Person> existingPerson = personRepositoryWrapper.findByPrimaryMobileNumber(primaryMobile);
            if (existingPerson.isPresent()) {
                throw PersonExceptionFactory.primaryMobileAlreadyExists(primaryMobile, messageSource);
            }
        }

        // Create Person entity
        Person person = new Person();
        person.setFirstName(request.getFirstName());
        person.setMiddleName(request.getMiddleName());
        person.setLastName(request.getLastName());
        person.setMobileNumbers(request.getMobileNumbers());
        person.setDateOfBirth(request.getDateOfBirth());
        person.setGender(request.getGender());
        
        // Generate display name
        person.setDisplayName(generateDisplayName(
                request.getFirstName(),
                request.getMiddleName(),
                request.getLastName()
        ));

        // Save person
        Person savedPerson = personRepositoryWrapper.saveWithException(person);
        
        return PersonCreateResponse.builder()
                .id(savedPerson.getId())
                .build();
    }

    @Override
    public void updatePerson(Long personId, PersonUpdateRequest request) {
        // Fetch existing person
        Person person = personRepositoryWrapper.findByIdWithException(personId);
        
        // Extract primary mobile number from request
        String primaryMobile = extractPrimaryMobileNumber(request.getMobileNumbers());
        
        // Check if primary mobile exists for a different person
        if (primaryMobile != null) {
            Optional<Person> existingPerson = personRepositoryWrapper.findByPrimaryMobileNumber(primaryMobile);
            if (existingPerson.isPresent() && !existingPerson.get().getId().equals(personId)) {
                throw PersonExceptionFactory.primaryMobileAlreadyExists(primaryMobile, messageSource);
            }
        }

        // Update all person fields
        person.setFirstName(request.getFirstName());
        person.setMiddleName(request.getMiddleName());
        person.setLastName(request.getLastName());
        person.setMobileNumbers(request.getMobileNumbers());
        person.setDateOfBirth(request.getDateOfBirth());
        person.setGender(request.getGender());
        
        // Generate display name
        person.setDisplayName(generateDisplayName(
                request.getFirstName(),
                request.getMiddleName(),
                request.getLastName()
        ));

        // Save person
        personRepositoryWrapper.saveWithException(person);
    }

    private String extractPrimaryMobileNumber(List<MobileNumberDetails> mobileNumbers) {
        if (mobileNumbers == null || mobileNumbers.isEmpty()) {
            return null;
        }
        
        return mobileNumbers.stream()
                .filter(m -> m.getIsPrimary() != null && m.getIsPrimary())
                .map(MobileNumberDetails::getNumber)
                .findFirst()
                .orElseThrow(()-> new BadRequestException("One Mobile Number is required to be marked as primary"));
    }

    private String generateDisplayName(String firstName, String middleName, String lastName) {
        StringBuilder displayName = new StringBuilder();
        
        if (firstName != null && !firstName.trim().isEmpty()) {
            displayName.append(firstName.trim());
        }

        if (middleName != null && !middleName.trim().isEmpty()) {
            if (!displayName.isEmpty()) displayName.append(" ");
            displayName.append(middleName.trim().charAt(0));
        }
        
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (!displayName.isEmpty()) displayName.append(" ");
            displayName.append(lastName.trim());
        }
        
        return !displayName.isEmpty() ? displayName.toString() : null;
    }
}

