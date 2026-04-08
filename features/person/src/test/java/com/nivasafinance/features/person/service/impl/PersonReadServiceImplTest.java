package com.nivasafinance.features.person.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.enums.AddressType;
import com.nivasafinance.common.enums.IdentifierType;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.enums.Gender;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonReadServiceImplTest {

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

    @InjectMocks
    private PersonReadServiceImpl personReadService;

    private static final Long TEST_PERSON_ID = 1L;
    private static final String TEST_MOBILE = "9876543210";
    private static final String TEST_EMAIL = "john@example.com";

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(TEST_PERSON_ID);
        person.setFirstName("John");
        person.setMiddleName("M");
        person.setLastName("Doe");
        person.setDisplayName("John M Doe");
        person.setEmail(TEST_EMAIL);
        person.setDateOfBirth(LocalDate.of(1990, 1, 15));
        person.setGender(Gender.MALE);
        person.setMobileNumbers(List.of(
                new MobileNumberDetails(TEST_MOBILE, true, false)
        ));
    }

    // ========== getPersonById ==========

    @Test
    void getPersonById_success_returnsMappedResponse() {
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        PersonResponse result = personReadService.getPersonById(TEST_PERSON_ID);

        assertNotNull(result);
        assertEquals(TEST_PERSON_ID, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("M", result.getMiddleName());
        assertEquals("Doe", result.getLastName());
        assertEquals("John M Doe", result.getDisplayName());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(Gender.MALE, result.getGender());
        assertEquals(LocalDate.of(1990, 1, 15), result.getDateOfBirth());
        assertNotNull(result.getMobileNumbers());
        assertEquals(1, result.getMobileNumbers().size());
        verify(personRepositoryWrapper).findByIdWithException(TEST_PERSON_ID);
    }

    @Test
    void getPersonById_mapsAllFieldsIncludingCbDetails() {
        Person.CreditBureauDetails cbDetails = Person.CreditBureauDetails.builder()
                .latestEnquiryId(100L)
                .latestSuccessEnquiryId(99L)
                .build();
        person.setCbDetails(cbDetails);
        person.setCbEnquiryId(List.of(99L, 100L));
        person.setExtData(Map.of("key", "value"));
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        PersonResponse result = personReadService.getPersonById(TEST_PERSON_ID);

        assertNotNull(result.getCbDetails());
        assertEquals(100L, result.getCbDetails().getLatestEnquiryId());
        assertEquals(99L, result.getCbDetails().getLatestSuccessEnquiryId());
        assertEquals(List.of(99L, 100L), result.getCbEnquiryId());
        assertEquals(Map.of("key", "value"), result.getExtData());
    }

    // ========== getPersonByPrimaryMobile ==========

    @Test
    void getPersonByPrimaryMobile_success_returnsResponse() {
        when(personRepositoryWrapper.findByPrimaryMobileNumberWithException(TEST_MOBILE)).thenReturn(person);

        PersonResponse result = personReadService.getPersonByPrimaryMobile(TEST_MOBILE);

        assertNotNull(result);
        assertEquals(TEST_PERSON_ID, result.getId());
        verify(personRepositoryWrapper).findByPrimaryMobileNumberWithException(TEST_MOBILE);
    }

    // ========== findPersonByPrimaryMobile ==========

    @Test
    void findPersonByPrimaryMobile_found_returnsOptionalWithResponse() {
        when(personRepositoryWrapper.findByPrimaryMobileNumber(TEST_MOBILE)).thenReturn(Optional.of(person));

        Optional<PersonResponse> result = personReadService.findPersonByPrimaryMobile(TEST_MOBILE);

        assertTrue(result.isPresent());
        assertEquals(TEST_PERSON_ID, result.get().getId());
    }

    @Test
    void findPersonByPrimaryMobile_notFound_returnsEmptyOptional() {
        when(personRepositoryWrapper.findByPrimaryMobileNumber("0000000000")).thenReturn(Optional.empty());

        Optional<PersonResponse> result = personReadService.findPersonByPrimaryMobile("0000000000");

        assertTrue(result.isEmpty());
    }

    // ========== findPersonByEmail ==========

    @Test
    void findPersonByEmail_found_returnsOptionalWithResponse() {
        when(personRepositoryWrapper.findPersonByEmail(TEST_EMAIL)).thenReturn(Optional.of(person));

        Optional<PersonResponse> result = personReadService.findPersonByEmail(TEST_EMAIL);

        assertTrue(result.isPresent());
        assertEquals(TEST_EMAIL, result.get().getEmail());
    }

    @Test
    void findPersonByEmail_notFound_returnsEmptyOptional() {
        when(personRepositoryWrapper.findPersonByEmail("unknown@test.com")).thenReturn(Optional.empty());

        Optional<PersonResponse> result = personReadService.findPersonByEmail("unknown@test.com");

        assertTrue(result.isEmpty());
    }

    // ========== getPersonByMobile ==========

    @Test
    void getPersonByMobile_multipleMatches_returnsAll() {
        Person person2 = new Person();
        person2.setId(2L);
        person2.setFirstName("Jane");
        when(personRepositoryWrapper.findByMobileNumberWithException(TEST_MOBILE))
                .thenReturn(List.of(person, person2));

        List<PersonResponse> result = personReadService.getPersonByMobile(TEST_MOBILE);

        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
    }

    @Test
    void getPersonByMobile_noMatches_returnsEmptyList() {
        when(personRepositoryWrapper.findByMobileNumberWithException("0000000000"))
                .thenReturn(List.of());

        List<PersonResponse> result = personReadService.getPersonByMobile("0000000000");

        assertTrue(result.isEmpty());
    }

    // ========== getAddresses ==========

    @Test
    void getAddresses_withAddresses_returnsList() {
        AddressData address = new AddressData();
        address.setId("addr-1");
        address.setAddressType(AddressType.CURRENT);
        person.setAddress(List.of(address));
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        List<AddressData> result = personReadService.getAddresses(TEST_PERSON_ID);

        assertEquals(1, result.size());
        assertEquals("addr-1", result.get(0).getId());
    }

    @Test
    void getAddresses_nullAddresses_returnsEmptyList() {
        person.setAddress(null);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        List<AddressData> result = personReadService.getAddresses(TEST_PERSON_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getAddress ==========

    @Test
    void getAddress_found_returnsAddress() {
        AddressData address = new AddressData();
        address.setId("addr-1");
        address.setAddressType(AddressType.CURRENT);
        address.setPincode("560001");
        person.setAddress(List.of(address));
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        AddressData result = personReadService.getAddress(TEST_PERSON_ID, "addr-1");

        assertNotNull(result);
        assertEquals("addr-1", result.getId());
        assertEquals("560001", result.getPincode());
    }

    @Test
    void getAddress_notFound_throwsResponseStatusException() {
        person.setAddress(List.of());
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> personReadService.getAddress(TEST_PERSON_ID, "nonexistent"));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void getAddress_nullAddressList_throwsResponseStatusException() {
        person.setAddress(null);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        assertThrows(ResponseStatusException.class,
                () -> personReadService.getAddress(TEST_PERSON_ID, "any-id"));
    }

    // ========== getIdentifiers ==========

    @Test
    void getIdentifiers_withIdentifiers_returnsList() {
        UUID identifierId = UUID.randomUUID();
        IdentifierData identifier = new IdentifierData();
        identifier.setId(identifierId);
        identifier.setType(IdentifierType.PAN);
        identifier.setIdentifier("ABCDE1234F");
        person.setIdentifiers(List.of(identifier));
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        List<IdentifierData> result = personReadService.getIdentifiers(TEST_PERSON_ID);

        assertEquals(1, result.size());
        assertEquals(IdentifierType.PAN, result.get(0).getType());
    }

    @Test
    void getIdentifiers_nullIdentifiers_returnsEmptyList() {
        person.setIdentifiers(null);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        List<IdentifierData> result = personReadService.getIdentifiers(TEST_PERSON_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getIdentifier ==========

    @Test
    void getIdentifier_found_returnsIdentifier() {
        UUID identifierId = UUID.randomUUID();
        IdentifierData identifier = new IdentifierData();
        identifier.setId(identifierId);
        identifier.setType(IdentifierType.AADHAAR);
        identifier.setIdentifier("123456789012");
        person.setIdentifiers(List.of(identifier));
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        IdentifierData result = personReadService.getIdentifier(TEST_PERSON_ID, identifierId);

        assertNotNull(result);
        assertEquals(identifierId, result.getId());
        assertEquals(IdentifierType.AADHAAR, result.getType());
    }

    @Test
    void getIdentifier_notFound_throwsResponseStatusException() {
        person.setIdentifiers(List.of());
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        UUID randomId = UUID.randomUUID();
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> personReadService.getIdentifier(TEST_PERSON_ID, randomId));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void getIdentifier_nullIdentifierList_throwsResponseStatusException() {
        person.setIdentifiers(null);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);

        assertThrows(ResponseStatusException.class,
                () -> personReadService.getIdentifier(TEST_PERSON_ID, UUID.randomUUID()));
    }
}
