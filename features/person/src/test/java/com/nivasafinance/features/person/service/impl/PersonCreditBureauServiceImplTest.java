package com.nivasafinance.features.person.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.enums.AddressType;
import com.nivasafinance.features.consent.dto.ConsentReceivedRequest;
import com.nivasafinance.features.consent.dto.CreateAndSendConsent;
import com.nivasafinance.features.consent.entity.Consent;
import com.nivasafinance.features.consent.service.ConsentWriteService;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryRequest;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import com.nivasafinance.features.creditbureau.service.CreditBureauReadService;
import com.nivasafinance.features.creditbureau.service.CreditBureauWriteService;
import com.nivasafinance.features.person.dto.CreditBureauEnquiryInitiationResult;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.dto.RecordCbConsentResult;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.enums.Gender;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonCreditBureauServiceImplTest {

    @Mock
    private PersonReadService personReadService;

    @Mock
    private CreditBureauWriteService creditBureauWriteService;

    @Mock
    private CreditBureauReadService creditBureauReadService;

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

    @Mock
    private ConsentWriteService consentWriteService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private PersonCreditBureauServiceImpl personCreditBureauService;

    @Captor
    private ArgumentCaptor<Person> personCaptor;

    @Captor
    private ArgumentCaptor<CreateAndSendConsent> consentRequestCaptor;

    private static final Long TEST_PERSON_ID = 1L;
    private static final Long TEST_ENQUIRY_ID = 100L;
    private static final Long TEST_CONSENT_ID = 50L;

    private PersonResponse personResponse;
    private Person person;
    private CreditBureauEnquiryRequest cbRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(personCreditBureauService, "stalePeriodInDays", 30);
        ReflectionTestUtils.setField(personCreditBureauService, "consentValidityPeriodInDays", 90);

        personResponse = PersonResponse.builder()
                .id(TEST_PERSON_ID)
                .firstName("John")
                .middleName("M")
                .lastName("Doe")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .mobileNumbers(List.of(new MobileNumberDetails("9876543210", true, false)))
                .build();

        person = new Person();
        person.setId(TEST_PERSON_ID);
        person.setFirstName("John");
        person.setCbEnquiryId(new ArrayList<>());

        cbRequest = new CreditBureauEnquiryRequest();
        cbRequest.setPersonId(TEST_PERSON_ID);
        cbRequest.setEntityType("LEAD");
        cbRequest.setEntityId(10L);
    }

    // ========== initiateCreditBureauEnquiry — STEP 1: Valid SUCCESS enquiry exists ==========

    @Test
    void initiateCreditBureauEnquiry_validSuccessEnquiryExists_returnsExistingEnquiry() {
        Map<String, Object> validEnquiry = new HashMap<>();
        validEnquiry.put("enquiry_id", TEST_ENQUIRY_ID);

        CreditBureauEnquiry enquiryEntity = new CreditBureauEnquiry();
        enquiryEntity.setId(TEST_ENQUIRY_ID);
        enquiryEntity.setStatus(CreditBureauEnquiryStatus.SUCCESS);
        enquiryEntity.setIdentifier(UUID.randomUUID());

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.of(validEnquiry));
        when(creditBureauReadService.getCbEnquiryEntityById(TEST_ENQUIRY_ID)).thenReturn(enquiryEntity);

        CreditBureauEnquiryInitiationResult result = personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        assertNotNull(result);
        assertNotNull(result.getResponse());
        assertNull(result.getAsyncPullFuture());
        verify(creditBureauWriteService, never()).initiateEnquiry(anyLong());
    }

    // ========== initiateCreditBureauEnquiry — STEP 2: In-progress enquiry ==========

    @Test
    void initiateCreditBureauEnquiry_initiatedEnquiryExists_returnsExisting() {
        personResponse.setCbDetails(Person.CreditBureauDetails.builder()
                .latestEnquiryId(TEST_ENQUIRY_ID)
                .build());
        CreditBureauEnquiryResponse existingResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .status(CreditBureauEnquiryStatus.INITIATED)
                .build();

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.empty());
        when(creditBureauReadService.getCbEnquiryById(TEST_ENQUIRY_ID))
                .thenReturn(Optional.of(existingResponse));

        CreditBureauEnquiryInitiationResult result = personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        assertNotNull(result);
        assertEquals(CreditBureauEnquiryStatus.INITIATED, result.getResponse().getStatus());
        assertNull(result.getAsyncPullFuture());
        verify(creditBureauWriteService, never()).initiateEnquiry(anyLong());
    }

    @Test
    void initiateCreditBureauEnquiry_processingEnquiryExists_returnsExisting() {
        personResponse.setCbDetails(Person.CreditBureauDetails.builder()
                .latestEnquiryId(TEST_ENQUIRY_ID)
                .build());
        CreditBureauEnquiryResponse existingResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .status(CreditBureauEnquiryStatus.PROCESSING)
                .build();

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.empty());
        when(creditBureauReadService.getCbEnquiryById(TEST_ENQUIRY_ID))
                .thenReturn(Optional.of(existingResponse));

        CreditBureauEnquiryInitiationResult result = personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        assertEquals(CreditBureauEnquiryStatus.PROCESSING, result.getResponse().getStatus());
        verify(creditBureauWriteService, never()).initiateEnquiry(anyLong());
    }

    // ========== initiateCreditBureauEnquiry — STEP 2 fallback: latestEnquiryId from cbEnquiryId list ==========

    @Test
    void initiateCreditBureauEnquiry_latestEnquiryFromCbEnquiryIdList_checksLastElement() {
        personResponse.setCbDetails(null);
        personResponse.setCbEnquiryId(List.of(50L, 60L, TEST_ENQUIRY_ID));

        CreditBureauEnquiryResponse existingResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .status(CreditBureauEnquiryStatus.INITIATED)
                .build();

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.empty());
        when(creditBureauReadService.getCbEnquiryById(TEST_ENQUIRY_ID))
                .thenReturn(Optional.of(existingResponse));

        CreditBureauEnquiryInitiationResult result = personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        assertEquals(CreditBureauEnquiryStatus.INITIATED, result.getResponse().getStatus());
    }

    @Test
    void initiateCreditBureauEnquiry_noLatestEnquiryId_proceedsToCreateNew() {
        personResponse.setCbDetails(null);
        personResponse.setCbEnquiryId(null);

        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiryResponse initiateResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .identifier(enquiryIdentifier)
                .status(CreditBureauEnquiryStatus.INITIATED)
                .build();

        Consent savedConsent = new Consent();
        savedConsent.setId(TEST_CONSENT_ID);
        savedConsent.setIdentifier(UUID.randomUUID());

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.empty());
        when(personRepositoryWrapper.findValidConsentForPerson(TEST_PERSON_ID, 90))
                .thenReturn(Optional.empty());
        when(creditBureauWriteService.initiateEnquiry(TEST_PERSON_ID)).thenReturn(initiateResponse);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(consentWriteService.createAndSendConsent(any(CreateAndSendConsent.class))).thenReturn(savedConsent);

        CreditBureauEnquiryInitiationResult result = personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        assertNotNull(result);
        assertNull(result.getAsyncPullFuture());
        verify(creditBureauWriteService).initiateEnquiry(TEST_PERSON_ID);
        verify(consentWriteService).createAndSendConsent(any(CreateAndSendConsent.class));
        verify(creditBureauWriteService).linkConsentToEnquiry(TEST_ENQUIRY_ID, TEST_CONSENT_ID);
    }

    // ========== initiateCreditBureauEnquiry — STEP 2: FAILED status proceeds to new enquiry ==========

    @Test
    void initiateCreditBureauEnquiry_latestEnquiryFailed_createsNewEnquiry() {
        personResponse.setCbDetails(Person.CreditBureauDetails.builder()
                .latestEnquiryId(TEST_ENQUIRY_ID)
                .build());
        CreditBureauEnquiryResponse failedResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .status(CreditBureauEnquiryStatus.FAILED)
                .build();

        Long newEnquiryId = 200L;
        UUID newEnquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiryResponse newInitiateResponse = CreditBureauEnquiryResponse.builder()
                .id(newEnquiryId)
                .identifier(newEnquiryIdentifier)
                .status(CreditBureauEnquiryStatus.INITIATED)
                .build();

        Consent savedConsent = new Consent();
        savedConsent.setId(TEST_CONSENT_ID);
        savedConsent.setIdentifier(UUID.randomUUID());

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.empty());
        when(creditBureauReadService.getCbEnquiryById(TEST_ENQUIRY_ID))
                .thenReturn(Optional.of(failedResponse));
        when(personRepositoryWrapper.findValidConsentForPerson(TEST_PERSON_ID, 90))
                .thenReturn(Optional.empty());
        when(creditBureauWriteService.initiateEnquiry(TEST_PERSON_ID)).thenReturn(newInitiateResponse);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(consentWriteService.createAndSendConsent(any(CreateAndSendConsent.class))).thenReturn(savedConsent);

        CreditBureauEnquiryInitiationResult result = personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        assertNotNull(result);
        assertEquals(newEnquiryId, result.getResponse().getId());
        verify(creditBureauWriteService).initiateEnquiry(TEST_PERSON_ID);
    }

    // ========== initiateCreditBureauEnquiry — STEP 5: Valid consent exists → trigger async pull ==========

    @Test
    void initiateCreditBureauEnquiry_validConsentExists_triggersAsyncPull() {
        personResponse.setCbDetails(null);
        personResponse.setCbEnquiryId(null);

        UUID consentIdentifier = UUID.randomUUID();
        Map<String, Object> validConsent = new HashMap<>();
        validConsent.put("consent_identifier", consentIdentifier);
        validConsent.put("consent_id", TEST_CONSENT_ID);

        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiryResponse initiateResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .identifier(enquiryIdentifier)
                .status(CreditBureauEnquiryStatus.INITIATED)
                .build();

        CreditBureauEnquiry enquiryEntity = new CreditBureauEnquiry();
        enquiryEntity.setId(TEST_ENQUIRY_ID);
        enquiryEntity.setIdentifier(enquiryIdentifier);

        CreditBureauEnquiryResponse pullResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .status(CreditBureauEnquiryStatus.SUCCESS)
                .build();

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.empty());
        when(personRepositoryWrapper.findValidConsentForPerson(TEST_PERSON_ID, 90))
                .thenReturn(Optional.of(validConsent));
        when(creditBureauWriteService.initiateEnquiry(TEST_PERSON_ID)).thenReturn(initiateResponse);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(creditBureauReadService.getCbEnquiryEntityById(TEST_ENQUIRY_ID)).thenReturn(enquiryEntity);
        when(personReadService.getAddresses(TEST_PERSON_ID)).thenReturn(List.of());
        when(personReadService.getIdentifiers(TEST_PERSON_ID)).thenReturn(List.of());
        when(creditBureauWriteService.executeCreditBureauFlowAsync(any(), eq(TEST_PERSON_ID), any()))
                .thenReturn(CompletableFuture.completedFuture(pullResponse));

        CreditBureauEnquiryInitiationResult result = personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        assertNotNull(result);
        assertNotNull(result.getAsyncPullFuture());
        assertEquals(consentIdentifier, result.getResponse().getConsentIdentifier());
        verify(creditBureauWriteService).linkConsentToEnquiry(TEST_ENQUIRY_ID, TEST_CONSENT_ID);
        verify(consentWriteService, never()).createAndSendConsent(any());
    }

    // ========== initiateCreditBureauEnquiry — STEP 5: No consent → create and send ==========

    @Test
    void initiateCreditBureauEnquiry_noConsent_createsConsentAndSendsLink() {
        personResponse.setCbDetails(null);
        personResponse.setCbEnquiryId(null);

        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiryResponse initiateResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .identifier(enquiryIdentifier)
                .status(CreditBureauEnquiryStatus.INITIATED)
                .build();

        UUID consentIdentifier = UUID.randomUUID();
        Consent savedConsent = new Consent();
        savedConsent.setId(TEST_CONSENT_ID);
        savedConsent.setIdentifier(consentIdentifier);

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.empty());
        when(personRepositoryWrapper.findValidConsentForPerson(TEST_PERSON_ID, 90))
                .thenReturn(Optional.empty());
        when(creditBureauWriteService.initiateEnquiry(TEST_PERSON_ID)).thenReturn(initiateResponse);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(consentWriteService.createAndSendConsent(any(CreateAndSendConsent.class))).thenReturn(savedConsent);

        CreditBureauEnquiryInitiationResult result = personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        assertNotNull(result);
        assertNull(result.getAsyncPullFuture());
        assertEquals(consentIdentifier, result.getResponse().getConsentIdentifier());
        verify(consentWriteService).createAndSendConsent(consentRequestCaptor.capture());
        CreateAndSendConsent capturedConsent = consentRequestCaptor.getValue();
        assertEquals(TEST_PERSON_ID, capturedConsent.getPersonId());
        assertEquals("CB", capturedConsent.getType());
        assertEquals(enquiryIdentifier, capturedConsent.getEnquiryIdentifier());
        assertEquals("9876543210", capturedConsent.getRecipientPhone());
        verify(creditBureauWriteService).linkConsentToEnquiry(TEST_ENQUIRY_ID, TEST_CONSENT_ID);
    }

    @Test
    void initiateCreditBureauEnquiry_noMobileNumbers_recipientPhoneIsNull() {
        personResponse.setMobileNumbers(null);
        personResponse.setCbDetails(null);
        personResponse.setCbEnquiryId(null);

        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiryResponse initiateResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .identifier(enquiryIdentifier)
                .status(CreditBureauEnquiryStatus.INITIATED)
                .build();

        Consent savedConsent = new Consent();
        savedConsent.setId(TEST_CONSENT_ID);
        savedConsent.setIdentifier(UUID.randomUUID());

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.empty());
        when(personRepositoryWrapper.findValidConsentForPerson(TEST_PERSON_ID, 90))
                .thenReturn(Optional.empty());
        when(creditBureauWriteService.initiateEnquiry(TEST_PERSON_ID)).thenReturn(initiateResponse);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(consentWriteService.createAndSendConsent(any(CreateAndSendConsent.class))).thenReturn(savedConsent);

        personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        verify(consentWriteService).createAndSendConsent(consentRequestCaptor.capture());
        assertNull(consentRequestCaptor.getValue().getRecipientPhone());
    }

    // ========== initiateCreditBureauEnquiry — updatePersonCbEnquiryIds ==========

    @Test
    void initiateCreditBureauEnquiry_updatesPersonCbEnquiryIdsAndLatestEnquiryId() {
        personResponse.setCbDetails(null);
        personResponse.setCbEnquiryId(null);
        person.setCbEnquiryId(null);
        person.setCbDetails(null);

        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiryResponse initiateResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .identifier(enquiryIdentifier)
                .status(CreditBureauEnquiryStatus.INITIATED)
                .build();

        Consent savedConsent = new Consent();
        savedConsent.setId(TEST_CONSENT_ID);
        savedConsent.setIdentifier(UUID.randomUUID());

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.empty());
        when(personRepositoryWrapper.findValidConsentForPerson(TEST_PERSON_ID, 90))
                .thenReturn(Optional.empty());
        when(creditBureauWriteService.initiateEnquiry(TEST_PERSON_ID)).thenReturn(initiateResponse);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(consentWriteService.createAndSendConsent(any(CreateAndSendConsent.class))).thenReturn(savedConsent);

        personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        // First save is from updatePersonCbEnquiryIds
        verify(personRepositoryWrapper, atLeastOnce()).saveWithException(personCaptor.capture());
        Person savedPerson = personCaptor.getAllValues().get(0);
        assertTrue(savedPerson.getCbEnquiryId().contains(TEST_ENQUIRY_ID));
        assertEquals(TEST_ENQUIRY_ID, savedPerson.getCbDetails().getLatestEnquiryId());
    }

    @Test
    void initiateCreditBureauEnquiry_existingEnquiryIds_doesNotDuplicate() {
        personResponse.setCbDetails(null);
        personResponse.setCbEnquiryId(null);
        person.setCbEnquiryId(new ArrayList<>(List.of(TEST_ENQUIRY_ID)));

        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiryResponse initiateResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .identifier(enquiryIdentifier)
                .status(CreditBureauEnquiryStatus.INITIATED)
                .build();

        Consent savedConsent = new Consent();
        savedConsent.setId(TEST_CONSENT_ID);
        savedConsent.setIdentifier(UUID.randomUUID());

        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30))
                .thenReturn(Optional.empty());
        when(personRepositoryWrapper.findValidConsentForPerson(TEST_PERSON_ID, 90))
                .thenReturn(Optional.empty());
        when(creditBureauWriteService.initiateEnquiry(TEST_PERSON_ID)).thenReturn(initiateResponse);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(consentWriteService.createAndSendConsent(any(CreateAndSendConsent.class))).thenReturn(savedConsent);

        personCreditBureauService.initiateCreditBureauEnquiry(cbRequest);

        verify(personRepositoryWrapper, atLeastOnce()).saveWithException(personCaptor.capture());
        Person savedPerson = personCaptor.getAllValues().get(0);
        assertEquals(1, savedPerson.getCbEnquiryId().size());
    }

    // ========== onConsentGranted ==========

    @Test
    void onConsentGranted_success_linksConsentAndTriggersPull() {
        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiry enquiry = new CreditBureauEnquiry();
        enquiry.setId(TEST_ENQUIRY_ID);
        enquiry.setIdentifier(enquiryIdentifier);
        enquiry.setStatus(CreditBureauEnquiryStatus.INITIATED);
        enquiry.setConsentId(null);

        CreditBureauEnquiryResponse pullResponse = CreditBureauEnquiryResponse.builder()
                .id(TEST_ENQUIRY_ID)
                .status(CreditBureauEnquiryStatus.SUCCESS)
                .build();

        person.setConsentDetails(null);

        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(personRepositoryWrapper.findPersonIdByCbEnquiryId(TEST_ENQUIRY_ID))
                .thenReturn(Optional.of(TEST_PERSON_ID));
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personReadService.getAddresses(TEST_PERSON_ID)).thenReturn(List.of());
        when(personReadService.getIdentifiers(TEST_PERSON_ID)).thenReturn(List.of());
        when(creditBureauWriteService.executeCreditBureauFlowAsync(any(), eq(TEST_PERSON_ID), any()))
                .thenReturn(CompletableFuture.completedFuture(pullResponse));

        CompletableFuture<CreditBureauEnquiryResponse> result =
                personCreditBureauService.onConsentGranted(TEST_CONSENT_ID, enquiryIdentifier, null);

        assertNotNull(result);
        verify(creditBureauWriteService).linkConsentToEnquiry(TEST_ENQUIRY_ID, TEST_CONSENT_ID);
        verify(personRepositoryWrapper, atLeastOnce()).saveWithException(personCaptor.capture());
        Person savedPerson = personCaptor.getAllValues().get(0);
        assertNotNull(savedPerson.getConsentDetails());
        assertEquals(1, savedPerson.getConsentDetails().size());
        assertEquals(TEST_CONSENT_ID, savedPerson.getConsentDetails().get(0).getId());
        assertEquals("CB", savedPerson.getConsentDetails().get(0).getType());
    }

    @Test
    void onConsentGranted_consentAlreadyLinkedAndMatches_proceedsWithPull() {
        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiry enquiry = new CreditBureauEnquiry();
        enquiry.setId(TEST_ENQUIRY_ID);
        enquiry.setIdentifier(enquiryIdentifier);
        enquiry.setStatus(CreditBureauEnquiryStatus.INITIATED);
        enquiry.setConsentId(TEST_CONSENT_ID);

        person.setConsentDetails(new ArrayList<>());

        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(personRepositoryWrapper.findPersonIdByCbEnquiryId(TEST_ENQUIRY_ID))
                .thenReturn(Optional.of(TEST_PERSON_ID));
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personReadService.getAddresses(TEST_PERSON_ID)).thenReturn(List.of());
        when(personReadService.getIdentifiers(TEST_PERSON_ID)).thenReturn(List.of());
        when(creditBureauWriteService.executeCreditBureauFlowAsync(any(), eq(TEST_PERSON_ID), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<CreditBureauEnquiryResponse> result =
                personCreditBureauService.onConsentGranted(TEST_CONSENT_ID, enquiryIdentifier, null);

        assertNotNull(result);
        verify(creditBureauWriteService, never()).linkConsentToEnquiry(anyLong(), anyLong());
    }

    @Test
    void onConsentGranted_consentMismatch_throwsException() {
        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiry enquiry = new CreditBureauEnquiry();
        enquiry.setId(TEST_ENQUIRY_ID);
        enquiry.setIdentifier(enquiryIdentifier);
        enquiry.setStatus(CreditBureauEnquiryStatus.INITIATED);
        enquiry.setConsentId(999L);

        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(personRepositoryWrapper.findPersonIdByCbEnquiryId(TEST_ENQUIRY_ID))
                .thenReturn(Optional.of(TEST_PERSON_ID));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Consent not linkable");

        assertThrows(Exception.class,
                () -> personCreditBureauService.onConsentGranted(TEST_CONSENT_ID, enquiryIdentifier, null));
    }

    @Test
    void onConsentGranted_enquiryNotInitiated_throwsException() {
        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiry enquiry = new CreditBureauEnquiry();
        enquiry.setId(TEST_ENQUIRY_ID);
        enquiry.setIdentifier(enquiryIdentifier);
        enquiry.setStatus(CreditBureauEnquiryStatus.SUCCESS);

        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(personRepositoryWrapper.findPersonIdByCbEnquiryId(TEST_ENQUIRY_ID))
                .thenReturn(Optional.of(TEST_PERSON_ID));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Not linkable");

        assertThrows(Exception.class,
                () -> personCreditBureauService.onConsentGranted(TEST_CONSENT_ID, enquiryIdentifier, null));
    }

    @Test
    void onConsentGranted_personNotFoundForEnquiry_throwsException() {
        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiry enquiry = new CreditBureauEnquiry();
        enquiry.setId(TEST_ENQUIRY_ID);
        enquiry.setIdentifier(enquiryIdentifier);

        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(personRepositoryWrapper.findPersonIdByCbEnquiryId(TEST_ENQUIRY_ID))
                .thenReturn(Optional.empty());
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Not found");

        assertThrows(Exception.class,
                () -> personCreditBureauService.onConsentGranted(TEST_CONSENT_ID, enquiryIdentifier, null));
    }

    @Test
    void onConsentGranted_withProvidedAddresses_usesProvidedAddresses() {
        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiry enquiry = new CreditBureauEnquiry();
        enquiry.setId(TEST_ENQUIRY_ID);
        enquiry.setIdentifier(enquiryIdentifier);
        enquiry.setStatus(CreditBureauEnquiryStatus.INITIATED);
        enquiry.setConsentId(null);

        person.setConsentDetails(new ArrayList<>());

        AddressData providedAddress = new AddressData();
        providedAddress.setAddressType(AddressType.CURRENT);
        providedAddress.setPincode("560001");
        List<AddressData> providedAddresses = List.of(providedAddress);

        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(personRepositoryWrapper.findPersonIdByCbEnquiryId(TEST_ENQUIRY_ID))
                .thenReturn(Optional.of(TEST_PERSON_ID));
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personReadService.getIdentifiers(TEST_PERSON_ID)).thenReturn(List.of());
        when(creditBureauWriteService.executeCreditBureauFlowAsync(any(), eq(TEST_PERSON_ID), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        personCreditBureauService.onConsentGranted(TEST_CONSENT_ID, enquiryIdentifier, providedAddresses);

        verify(personReadService, never()).getAddresses(anyLong());
        verify(creditBureauWriteService).linkConsentToEnquiry(TEST_ENQUIRY_ID, TEST_CONSENT_ID);
    }

    @Test
    void onConsentGranted_existingConsentDetails_appendsNewConsent() {
        UUID enquiryIdentifier = UUID.randomUUID();
        CreditBureauEnquiry enquiry = new CreditBureauEnquiry();
        enquiry.setId(TEST_ENQUIRY_ID);
        enquiry.setIdentifier(enquiryIdentifier);
        enquiry.setStatus(CreditBureauEnquiryStatus.INITIATED);
        enquiry.setConsentId(null);

        Person.ConsentInfo existingConsent = new Person.ConsentInfo(10L, "OTHER");
        person.setConsentDetails(new ArrayList<>(List.of(existingConsent)));

        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(personRepositoryWrapper.findPersonIdByCbEnquiryId(TEST_ENQUIRY_ID))
                .thenReturn(Optional.of(TEST_PERSON_ID));
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);
        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(personResponse);
        when(personReadService.getAddresses(TEST_PERSON_ID)).thenReturn(List.of());
        when(personReadService.getIdentifiers(TEST_PERSON_ID)).thenReturn(List.of());
        when(creditBureauWriteService.executeCreditBureauFlowAsync(any(), eq(TEST_PERSON_ID), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        personCreditBureauService.onConsentGranted(TEST_CONSENT_ID, enquiryIdentifier, null);

        verify(personRepositoryWrapper, atLeastOnce()).saveWithException(personCaptor.capture());
        Person savedPerson = personCaptor.getAllValues().get(0);
        assertEquals(2, savedPerson.getConsentDetails().size());
        assertEquals(10L, savedPerson.getConsentDetails().get(0).getId());
        assertEquals(TEST_CONSENT_ID, savedPerson.getConsentDetails().get(1).getId());
    }

    // ========== recordCbConsentReceived ==========

    @Test
    void recordCbConsentReceived_success_createsConsentAndUpdatesPersonDetails() {
        UUID consentIdentifier = UUID.randomUUID();
        Consent savedConsent = new Consent();
        savedConsent.setId(TEST_CONSENT_ID);
        savedConsent.setIdentifier(consentIdentifier);

        person.setConsentDetails(null);

        when(consentWriteService.createConsentReceived(any(ConsentReceivedRequest.class))).thenReturn(savedConsent);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        RecordCbConsentResult result = personCreditBureauService.recordCbConsentReceived(TEST_PERSON_ID);

        assertNotNull(result);
        assertEquals(consentIdentifier, result.getConsentIdentifier());
        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        Person savedPerson = personCaptor.getValue();
        assertNotNull(savedPerson.getConsentDetails());
        assertEquals(1, savedPerson.getConsentDetails().size());
        assertEquals(TEST_CONSENT_ID, savedPerson.getConsentDetails().get(0).getId());
        assertEquals("CB", savedPerson.getConsentDetails().get(0).getType());
    }

    @Test
    void recordCbConsentReceived_existingConsentDetails_appendsNewConsent() {
        UUID consentIdentifier = UUID.randomUUID();
        Consent savedConsent = new Consent();
        savedConsent.setId(TEST_CONSENT_ID);
        savedConsent.setIdentifier(consentIdentifier);

        Person.ConsentInfo existingConsent = new Person.ConsentInfo(10L, "OTHER");
        person.setConsentDetails(new ArrayList<>(List.of(existingConsent)));

        when(consentWriteService.createConsentReceived(any(ConsentReceivedRequest.class))).thenReturn(savedConsent);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(person);
        when(personRepositoryWrapper.saveWithException(any(Person.class))).thenReturn(person);

        RecordCbConsentResult result = personCreditBureauService.recordCbConsentReceived(TEST_PERSON_ID);

        assertNotNull(result);
        verify(personRepositoryWrapper).saveWithException(personCaptor.capture());
        assertEquals(2, personCaptor.getValue().getConsentDetails().size());
    }
}
