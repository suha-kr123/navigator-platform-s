package com.nivasafinance.features.person.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.common.enums.AddressType;
import com.nivasafinance.common.enums.IdentifierType;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.identifier.service.IdentifierService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.enums.Gender;
import com.nivasafinance.features.person.exception.PersonPrimaryMobileAlreadyExistsException;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonWriteServiceImplTest {

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private AddressDataService addressDataService;

    @Mock
    private IdentifierService identifierService;

    @InjectMocks
    private PersonWriteServiceImpl personWriteService;

    @Captor
    private ArgumentCaptor<Person> personCaptor;

    private static final Long TEST_PERSON_ID = 1L;
    private static final String TEST_MOBILE = "9876543210";

    private Person person;
    private MobileNumberDetails primaryMobile;

    @BeforeEach
    void setUp() {
        primaryMobile = new MobileNumberDetails(TEST_MOBILE, true, false);

        person = new Person();
        person.setId(TEST_PERSON_ID);
        person.setFirstName("John");
        person.setMiddleName("Michael");
        person.setLastName("Doe");
        person.setMobileNumbers(List.of(primaryMobile));
        person.setGender(Gender.MALE);
        person.setDateOfBirth(LocalDate.of(1990, 1, 15));
    }

    // ========== createPerson ==========

    @Test
    void createPerson_success_savesAndReturnsId() {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("John")
                .middleName("Michael")
                .lastName("Doe")
                .email("john@example.com")
                .mobileNumbers(List.of(primaryMobile))
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .gender(Gender.MALE)
                .build();

        when(personRepositoryWrapper.findByPrimaryMobileNumber(TEST_MOBILE)).thenReturn(Optional.empty());
        Person savedPerson = new Person();
        savedPerson.setId(TEST_PERSON_ID);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(savedPerson);

        PersonCreateResponse result = personWriteService.createPerson(request);

        assertNotNull(result);
        assertEquals(TEST_PERSON_ID, result.getId());
        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        Person captured = personCaptor.getValue();
        assertEquals("John M Doe", captured.getDisplayName());
        assertEquals("john@example.com", captured.getEmail());
        assertEquals(Gender.MALE, captured.getGender());
    }

    @Test
    void createPerson_primaryMobileAlreadyExists_throwsException() {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("Jane")
                .mobileNumbers(List.of(primaryMobile))
                .build();

        when(personRepositoryWrapper.findByPrimaryMobileNumber(TEST_MOBILE)).thenReturn(Optional.of(person));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Mobile already exists");

        assertThrows(PersonPrimaryMobileAlreadyExistsException.class,
                () -> personWriteService.createPerson(request));

        verify(personRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void createPerson_nullMobileNumbers_savesWithNullPrimaryMobile() {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .mobileNumbers(null)
                .build();

        Person savedPerson = new Person();
        savedPerson.setId(TEST_PERSON_ID);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(savedPerson);

        PersonCreateResponse result = personWriteService.createPerson(request);

        assertNotNull(result);
        verify(personRepositoryWrapper, never()).findByPrimaryMobileNumber(any());
    }

    @Test
    void createPerson_emptyMobileNumbers_savesWithNullPrimaryMobile() {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("John")
                .mobileNumbers(List.of())
                .build();

        Person savedPerson = new Person();
        savedPerson.setId(TEST_PERSON_ID);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(savedPerson);

        PersonCreateResponse result = personWriteService.createPerson(request);

        assertNotNull(result);
        verify(personRepositoryWrapper, never()).findByPrimaryMobileNumber(any());
    }

    @Test
    void createPerson_noPrimaryMobileInList_throwsBadRequestException() {
        MobileNumberDetails nonPrimary = new MobileNumberDetails("1234567890", false, false);
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("John")
                .mobileNumbers(List.of(nonPrimary))
                .build();

        assertThrows(BadRequestException.class,
                () -> personWriteService.createPerson(request));
    }

    @Test
    void createPerson_displayNameGeneration_onlyFirstName() {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("John")
                .middleName(null)
                .lastName(null)
                .mobileNumbers(null)
                .build();

        Person savedPerson = new Person();
        savedPerson.setId(TEST_PERSON_ID);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(savedPerson);

        personWriteService.createPerson(request);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertEquals("John", personCaptor.getValue().getDisplayName());
    }

    @Test
    void createPerson_displayNameGeneration_firstAndLast() {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("John")
                .middleName(null)
                .lastName("Doe")
                .mobileNumbers(null)
                .build();

        Person savedPerson = new Person();
        savedPerson.setId(TEST_PERSON_ID);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(savedPerson);

        personWriteService.createPerson(request);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertEquals("John Doe", personCaptor.getValue().getDisplayName());
    }

    @Test
    void createPerson_displayNameGeneration_allNamesBlank_returnsNull() {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("")
                .middleName("  ")
                .lastName("")
                .mobileNumbers(null)
                .build();

        Person savedPerson = new Person();
        savedPerson.setId(TEST_PERSON_ID);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(savedPerson);

        personWriteService.createPerson(request);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertNull(personCaptor.getValue().getDisplayName());
    }

    @Test
    void createPerson_displayNameGeneration_middleInitialOnly() {
        PersonCreateRequest request = PersonCreateRequest.builder()
                .firstName("John")
                .middleName("Michael")
                .lastName("Doe")
                .mobileNumbers(null)
                .build();

        Person savedPerson = new Person();
        savedPerson.setId(TEST_PERSON_ID);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(savedPerson);

        personWriteService.createPerson(request);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertEquals("John M Doe", personCaptor.getValue().getDisplayName());
    }

    // ========== updatePerson ==========

    @Test
    void updatePerson_success_updatesAllFields() {
        MobileNumberDetails newPrimary = new MobileNumberDetails("1111111111", true, true);
        PersonUpdateRequest request = PersonUpdateRequest.builder()
                .firstName("Jane")
                .middleName("Marie")
                .lastName("Smith")
                .email("jane@example.com")
                .mobileNumbers(List.of(newPrimary))
                .dateOfBirth(LocalDate.of(1995, 6, 20))
                .gender(Gender.FEMALE)
                .build();

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("1111111111")).thenReturn(Optional.empty());
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.updatePerson(TEST_PERSON_ID, request);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        Person captured = personCaptor.getValue();
        assertEquals("Jane", captured.getFirstName());
        assertEquals("Marie", captured.getMiddleName());
        assertEquals("Smith", captured.getLastName());
        assertEquals("jane@example.com", captured.getEmail());
        assertEquals(Gender.FEMALE, captured.getGender());
        assertEquals("Jane M Smith", captured.getDisplayName());
    }

    @Test
    void updatePerson_samePrimaryMobileSamePerson_succeeds() {
        PersonUpdateRequest request = PersonUpdateRequest.builder()
                .firstName("John")
                .mobileNumbers(List.of(primaryMobile))
                .build();

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.findByPrimaryMobileNumber(TEST_MOBILE)).thenReturn(Optional.of(person));
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        assertDoesNotThrow(() -> personWriteService.updatePerson(TEST_PERSON_ID, request));
        verify(personRepositoryWrapper).saveWithException(any(Person.class));
    }

    @Test
    void updatePerson_primaryMobileOwnedByDifferentPerson_throwsException() {
        Person otherPerson = new Person();
        otherPerson.setId(999L);

        PersonUpdateRequest request = PersonUpdateRequest.builder()
                .firstName("John")
                .mobileNumbers(List.of(primaryMobile))
                .build();

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.findByPrimaryMobileNumber(TEST_MOBILE)).thenReturn(Optional.of(otherPerson));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Mobile already exists");

        assertThrows(PersonPrimaryMobileAlreadyExistsException.class,
                () -> personWriteService.updatePerson(TEST_PERSON_ID, request));

        verify(personRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void updatePerson_nullMobileNumbers_skipsUniquenessCheck() {
        PersonUpdateRequest request = PersonUpdateRequest.builder()
                .firstName("John")
                .mobileNumbers(null)
                .build();

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.updatePerson(TEST_PERSON_ID, request);

        verify(personRepositoryWrapper, never()).findByPrimaryMobileNumber(any());
    }

    // ========== addAddress ==========

    @Test
    void addAddress_newAddressType_addsToList() {
        AddressRequest request = new AddressRequest();
        request.setAddressType(AddressType.CURRENT);
        person.setAddress(null);

        AddressData builtAddress = new AddressData();
        builtAddress.setAddressType(AddressType.CURRENT);

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(addressDataService.createAddressData(request)).thenReturn(builtAddress);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        String addressId = personWriteService.addAddress(TEST_PERSON_ID, request);

        assertNotNull(addressId);
        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertNotNull(personCaptor.getValue().getAddress());
        assertEquals(1, personCaptor.getValue().getAddress().size());
    }

    @Test
    void addAddress_existingAddressType_replacesExisting() {
        AddressData existingAddress = new AddressData();
        existingAddress.setId("existing-id");
        existingAddress.setAddressType(AddressType.CURRENT);
        existingAddress.setPincode("560001");
        person.setAddress(new ArrayList<>(List.of(existingAddress)));

        AddressRequest request = new AddressRequest();
        request.setAddressType(AddressType.CURRENT);

        AddressData builtAddress = new AddressData();
        builtAddress.setAddressType(AddressType.CURRENT);

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(addressDataService.createAddressData(request)).thenReturn(builtAddress);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        String addressId = personWriteService.addAddress(TEST_PERSON_ID, request);

        assertEquals("existing-id", addressId);
        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertEquals(1, personCaptor.getValue().getAddress().size());
    }

    @Test
    void addAddress_nullAddressType_throwsException() {
        AddressRequest request = new AddressRequest();
        request.setAddressType(null);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Address type mandatory");

        assertThrows(BadRequestException.class,
                () -> personWriteService.addAddress(TEST_PERSON_ID, request));

        verify(personRepositoryWrapper, never()).findByIdWithException(any());
    }

    // ========== updateAddress ==========

    @Test
    void updateAddress_success_updatesExistingAddress() {
        String addressId = "addr-1";
        AddressData existingAddress = new AddressData();
        existingAddress.setId(addressId);
        existingAddress.setAddressType(AddressType.CURRENT);
        person.setAddress(new ArrayList<>(List.of(existingAddress)));

        AddressRequest request = new AddressRequest();
        request.setAddressType(AddressType.CURRENT);

        AddressData builtAddress = new AddressData();
        builtAddress.setAddressType(AddressType.CURRENT);

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(addressDataService.createAddressData(request)).thenReturn(builtAddress);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        AddressData result = personWriteService.updateAddress(TEST_PERSON_ID, addressId, request);

        assertNotNull(result);
        assertEquals(addressId, result.getId());
    }

    @Test
    void updateAddress_addressNotFound_throwsResponseStatusException() {
        person.setAddress(new ArrayList<>());
        AddressRequest request = new AddressRequest();
        request.setAddressType(AddressType.CURRENT);

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        assertThrows(ResponseStatusException.class,
                () -> personWriteService.updateAddress(TEST_PERSON_ID, "nonexistent", request));
    }

    @Test
    void updateAddress_nullAddressType_throwsException() {
        AddressRequest request = new AddressRequest();
        request.setAddressType(null);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Address type mandatory");

        assertThrows(BadRequestException.class,
                () -> personWriteService.updateAddress(TEST_PERSON_ID, "addr-1", request));
    }

    // ========== addIdentifier ==========

    @Test
    void addIdentifier_success_addsToList() {
        person.setIdentifiers(null);
        IdentifierRequest request = new IdentifierRequest();
        request.setType(IdentifierType.PAN);
        request.setIdentifier("ABCDE1234F");

        IdentifierData createdIdentifier = new IdentifierData();
        createdIdentifier.setId(UUID.randomUUID());
        createdIdentifier.setType(IdentifierType.PAN);
        createdIdentifier.setIdentifier("ABCDE1234F");

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(identifierService.createIdentifierData(request)).thenReturn(createdIdentifier);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        IdentifierData result = personWriteService.addIdentifier(TEST_PERSON_ID, request);

        assertNotNull(result);
        assertEquals(IdentifierType.PAN, result.getType());
        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertNotNull(personCaptor.getValue().getIdentifiers());
    }

    @Test
    void addIdentifier_existingIdentifiers_appendsToList() {
        UUID existingId = UUID.randomUUID();
        IdentifierData existing = new IdentifierData();
        existing.setId(existingId);
        existing.setType(IdentifierType.PAN);
        person.setIdentifiers(new ArrayList<>(List.of(existing)));

        IdentifierRequest request = new IdentifierRequest();
        request.setType(IdentifierType.AADHAAR);
        request.setIdentifier("123456789012");

        IdentifierData newIdentifier = new IdentifierData();
        newIdentifier.setId(UUID.randomUUID());
        newIdentifier.setType(IdentifierType.AADHAAR);

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(identifierService.createIdentifierData(request)).thenReturn(newIdentifier);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.addIdentifier(TEST_PERSON_ID, request);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertEquals(2, personCaptor.getValue().getIdentifiers().size());
    }

    // ========== updateIdentifier ==========

    @Test
    void updateIdentifier_success_updatesExisting() {
        UUID identifierId = UUID.randomUUID();
        IdentifierData existing = new IdentifierData();
        existing.setId(identifierId);
        existing.setType(IdentifierType.PAN);
        existing.setIdentifier("ABCDE1234F");
        person.setIdentifiers(new ArrayList<>(List.of(existing)));

        IdentifierRequest request = new IdentifierRequest();
        request.setType(IdentifierType.PAN);
        request.setIdentifier("XYZAB9876G");

        IdentifierData updatedData = new IdentifierData();
        updatedData.setType(IdentifierType.PAN);
        updatedData.setIdentifier("XYZAB9876G");

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(identifierService.createIdentifierData(request)).thenReturn(updatedData);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.updateIdentifier(TEST_PERSON_ID, identifierId, request);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        List<IdentifierData> savedIdentifiers = personCaptor.getValue().getIdentifiers();
        assertEquals(1, savedIdentifiers.size());
        assertEquals(identifierId, savedIdentifiers.get(0).getId());
        assertEquals("XYZAB9876G", savedIdentifiers.get(0).getIdentifier());
    }

    @Test
    void updateIdentifier_notFound_throwsResponseStatusException() {
        person.setIdentifiers(new ArrayList<>());

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        assertThrows(ResponseStatusException.class,
                () -> personWriteService.updateIdentifier(TEST_PERSON_ID, UUID.randomUUID(), new IdentifierRequest()));
    }

    // ========== deleteIdentifier ==========

    @Test
    void deleteIdentifier_success_removesFromList() {
        UUID identifierId = UUID.randomUUID();
        IdentifierData existing = new IdentifierData();
        existing.setId(identifierId);
        existing.setType(IdentifierType.PAN);
        person.setIdentifiers(new ArrayList<>(List.of(existing)));

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.deleteIdentifier(TEST_PERSON_ID, identifierId);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertNull(personCaptor.getValue().getIdentifiers());
    }

    @Test
    void deleteIdentifier_notFound_throwsResponseStatusException() {
        person.setIdentifiers(new ArrayList<>());

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        assertThrows(ResponseStatusException.class,
                () -> personWriteService.deleteIdentifier(TEST_PERSON_ID, UUID.randomUUID()));
    }

    @Test
    void deleteIdentifier_multipleIdentifiers_removesOnlyTarget() {
        UUID targetId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        IdentifierData target = new IdentifierData();
        target.setId(targetId);
        target.setType(IdentifierType.PAN);
        IdentifierData other = new IdentifierData();
        other.setId(otherId);
        other.setType(IdentifierType.AADHAAR);
        person.setIdentifiers(new ArrayList<>(List.of(target, other)));

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.deleteIdentifier(TEST_PERSON_ID, targetId);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        List<IdentifierData> remaining = personCaptor.getValue().getIdentifiers();
        assertEquals(1, remaining.size());
        assertEquals(otherId, remaining.get(0).getId());
    }

    // ========== updateDateOfBirthIfAbsent ==========

    @Test
    void updateDateOfBirthIfAbsent_nullDateOfBirth_doesNothing() {
        personWriteService.updateDateOfBirthIfAbsent(TEST_PERSON_ID, null);

        verifyNoInteractions(personRepositoryWrapper);
    }

    @Test
    void updateDateOfBirthIfAbsent_personAlreadyHasDob_doesNotUpdate() {
        person.setDateOfBirth(LocalDate.of(1990, 1, 15));
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        personWriteService.updateDateOfBirthIfAbsent(TEST_PERSON_ID, LocalDate.of(2000, 6, 1));

        verify(personRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void updateDateOfBirthIfAbsent_personHasNoDob_updatesDob() {
        person.setDateOfBirth(null);
        LocalDate newDob = LocalDate.of(1995, 3, 10);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.updateDateOfBirthIfAbsent(TEST_PERSON_ID, newDob);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertEquals(newDob, personCaptor.getValue().getDateOfBirth());
    }

    // ========== updateCreditBureauFields ==========

    @Test
    void updateCreditBureauFields_bothFieldsProvided_updatesBoth() {
        List<Long> enquiryIds = List.of(100L, 200L);
        Person.CreditBureauDetails cbDetails = Person.CreditBureauDetails.builder()
                .latestEnquiryId(200L)
                .latestSuccessEnquiryId(100L)
                .build();

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.updateCreditBureauFields(TEST_PERSON_ID, enquiryIds, cbDetails);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        Person captured = personCaptor.getValue();
        assertEquals(enquiryIds, captured.getCbEnquiryId());
        assertEquals(cbDetails, captured.getCbDetails());
    }

    @Test
    void updateCreditBureauFields_nullEnquiryIds_onlyUpdatesCbDetails() {
        Person.CreditBureauDetails cbDetails = Person.CreditBureauDetails.builder()
                .latestEnquiryId(300L)
                .build();
        person.setCbEnquiryId(List.of(100L));

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.updateCreditBureauFields(TEST_PERSON_ID, null, cbDetails);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        Person captured = personCaptor.getValue();
        assertEquals(List.of(100L), captured.getCbEnquiryId());
        assertEquals(cbDetails, captured.getCbDetails());
    }

    @Test
    void updateCreditBureauFields_nullCbDetails_onlyUpdatesEnquiryIds() {
        List<Long> enquiryIds = List.of(500L);
        Person.CreditBureauDetails existingCbDetails = Person.CreditBureauDetails.builder()
                .latestEnquiryId(100L)
                .build();
        person.setCbDetails(existingCbDetails);

        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.updateCreditBureauFields(TEST_PERSON_ID, enquiryIds, null);

        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        Person captured = personCaptor.getValue();
        assertEquals(enquiryIds, captured.getCbEnquiryId());
        assertEquals(existingCbDetails, captured.getCbDetails());
    }

    @Test
    void updateCreditBureauFields_bothNull_stillSaves() {
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        personWriteService.updateCreditBureauFields(TEST_PERSON_ID, null, null);

        verify(personRepositoryWrapper).saveWithException(any(Person.class));
    }
}
