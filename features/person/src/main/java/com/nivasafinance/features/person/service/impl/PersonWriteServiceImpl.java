package com.nivasafinance.features.person.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.exception.PersonExceptionFactory;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.identifier.service.IdentifierService;
import com.nivasafinance.features.person.service.PersonWriteService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class PersonWriteServiceImpl implements PersonWriteService {

    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final MessageSource messageSource;
    private final AddressDataService addressDataService;
    private final IdentifierService identifierService;

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

    @Override
    public String addAddress(Long personId, AddressRequest request) {

        if (request.getAddressType() == null) {
            throw PersonExceptionFactory.addressTypeMandatory(messageSource);
        } 

        Person person = personRepositoryWrapper.findByIdWithException(personId);
        List<AddressData> addresses = getAddresses(person);

        AddressData addressData = buildAddressData(request, null);
        // Ensure ID is set before adding to list
        if (addressData.getId() == null) {
            addressData.setId(UUID.randomUUID().toString());
        }
        addresses.add(addressData);

        saveAddresses(person, addresses);

        return addressData.getId();
    }

    @Override
    public AddressData updateAddress(Long personId, String addressId, AddressRequest request) {


        if (request.getAddressType() == null) {
            throw PersonExceptionFactory.addressTypeMandatory(messageSource);
        } 

        Person person = personRepositoryWrapper.findByIdWithException(personId);
        List<AddressData> addresses = getAddresses(person);

        AddressData existing = addresses.stream()
                .filter(address -> addressId.equals(address.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Address not found for person"));

        AddressData updatedAddress = buildAddressData(request, existing.getId());

        int index = addresses.indexOf(existing);
        addresses.set(index, updatedAddress);

        saveAddresses(person, addresses);

        return updatedAddress;
    }

    private List<AddressData> getAddresses(Person person) {
        List<AddressData> addresses = person.getAddress();
        return addresses == null ? new ArrayList<>() : new ArrayList<>(addresses);
    }

    private void saveAddresses(Person person, List<AddressData> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            person.setAddress(null);
        } else {
            person.setAddress(new ArrayList<>(addresses));
        }
        personRepositoryWrapper.saveWithException(person);
    }

    private AddressData buildAddressData(AddressRequest request, String addressId) {
        AddressData addressData = addressDataService.createAddressData(request);
        addressData.setId(addressId != null ? addressId : UUID.randomUUID().toString());
        addressData.setAddressType(request.getAddressType());
        return addressData;
    }

    @Override
    public IdentifierData addIdentifier(Long personId, IdentifierRequest request) {
        Person person = personRepositoryWrapper.findByIdWithException(personId);
        List<IdentifierData> identifiers = getIdentifiers(person);

        IdentifierData identifierData = identifierService.createIdentifierData(request);
        identifiers.add(identifierData);

        saveIdentifiers(person, identifiers);

        return identifierData;
    }

    @Override
    public void updateIdentifier(Long personId, UUID identifierId, IdentifierRequest request) {
        Person person = personRepositoryWrapper.findByIdWithException(personId);
        List<IdentifierData> identifiers = getIdentifiers(person);

        IdentifierData existing = identifiers.stream()
                .filter(identifier -> identifierId.equals(identifier.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Identifier not found for person"));

        IdentifierData updatedIdentifier = identifierService.createIdentifierData(request);
        updatedIdentifier.setId(existing.getId());

        int index = identifiers.indexOf(existing);
        identifiers.set(index, updatedIdentifier);

        saveIdentifiers(person, identifiers);

    }

    @Override
    public void deleteIdentifier(Long personId, UUID identifierId) {
        Person person = personRepositoryWrapper.findByIdWithException(personId);
        List<IdentifierData> identifiers = getIdentifiers(person);

        boolean removed = identifiers.removeIf(identifier -> identifierId.equals(identifier.getId()));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Identifier not found for person");
        }

        saveIdentifiers(person, identifiers);
    }

    private List<IdentifierData> getIdentifiers(Person person) {
        List<IdentifierData> identifiers = person.getIdentifiers();
        return identifiers == null ? new ArrayList<>() : identifiers;
    }

    private void saveIdentifiers(Person person, List<IdentifierData> identifiers) {
        if (identifiers == null || identifiers.isEmpty()) {
            person.setIdentifiers(null);
        } else {
            person.setIdentifiers(new ArrayList<>(identifiers));
        }
        personRepositoryWrapper.saveWithException(person);
    }

    @Override
    public void updateCreditBureauFields(Long personId, List<Long> cbEnquiryIds, Person.CreditBureauDetails cbDetails) {
        Person person = personRepositoryWrapper.findByIdWithException(personId);

        if (cbEnquiryIds != null) {
            person.setCbEnquiryId(cbEnquiryIds);
        }

        if (cbDetails != null) {
            person.setCbDetails(cbDetails);
        }

        personRepositoryWrapper.saveWithException(person);
    }
}

