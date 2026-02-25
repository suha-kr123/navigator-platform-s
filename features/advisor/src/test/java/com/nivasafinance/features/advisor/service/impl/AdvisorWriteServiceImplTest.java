package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.events.payload.AdvisorCreationEventPayload;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.referral.service.ReferralCodeRegistryService;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelWriteService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorWriteServiceImplTest {

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private PersonWriteService personWriteService;

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

    @Mock
    private SourcingChannelWriteService sourcingChannelWriteService;

    @Mock
    private CodeMasterService codeMasterService;

    @Mock
    private OfficeReadService officeReadService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ReferralCodeRegistryService referralCodeRegistryService;

    @Mock
    private UserReadService userReadService;

    @Mock
    private UserWriteService userWriteService;

    @InjectMocks
    private AdvisorWriteServiceImpl advisorWriteService;

    private static final Long TEST_PERSON_ID = 2L;
    private static final String TEST_ADVISOR_USERNAME = "advisorUser";

    private UUID identifier;
    private Advisor advisor;
    private CreateAdvisorRequest createRequest;

    @BeforeEach
    void setUp() {
        identifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(identifier);
        advisor.setUsername(TEST_ADVISOR_USERNAME);
        advisor.setStatus(AdvisorStatus.CREATED);
        advisor.setOfficeKey("HQ");

        MobileNumberDetails mobileDetails = new MobileNumberDetails();
        mobileDetails.setMobileNumber("9876543210");
        mobileDetails.setIsPrimary(true);
        createRequest = new CreateAdvisorRequest();
        createRequest.setMobileNumberDetails(mobileDetails);
        createRequest.setOfficeKey(null);
    }

    @Test
    void createAdvisor_newPerson_success() {
        when(userReadService.findUsersByPersonPhoneNumber("9876543210")).thenReturn(Collections.emptyList());
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        PersonCreateResponse personResponse = PersonCreateResponse.builder().id(TEST_PERSON_ID).build();
        when(personWriteService.createPerson(any(PersonCreateRequest.class))).thenReturn(personResponse);
        when(advisorRepositoryWrapper.findByUsername("9876543210")).thenReturn(Optional.empty());
        when(userWriteService.createUserForExistingPerson(any(), eq(TEST_PERSON_ID))).thenReturn(UserResponse.builder().build());
        Advisor savedAdvisor = new Advisor();
        savedAdvisor.setId(1L);
        savedAdvisor.setIdentifier(identifier);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(savedAdvisor);
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.ADVISOR), any())).thenReturn(null);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            UUID result = advisorWriteService.createAdvisor(createRequest);

            assertNotNull(result);
            assertEquals(identifier, result);
            verify(personWriteService).createPerson(any(PersonCreateRequest.class));
            verify(advisorRepositoryWrapper, atLeast(1)).saveWithException(any(Advisor.class));
            verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));

            ArgumentCaptor<SystemEvent<?>> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
            verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(BusinessEvent.ADVISOR_CREATED.toString(), eventCaptor.getValue().getEventType());
            assertTrue(eventCaptor.getValue().getPayload() instanceof AdvisorCreationEventPayload);
        }
    }

    @Test
    void createAdvisor_existingPersonNoAdvisor_success() {
        Person existingPerson = new Person();
        existingPerson.setId(TEST_PERSON_ID);
        when(userReadService.findUsersByPersonPhoneNumber("9876543210")).thenReturn(Collections.emptyList());
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.of(existingPerson));
        when(advisorRepositoryWrapper.findByUsername("9876543210")).thenReturn(Optional.empty());
        when(userWriteService.createUserForExistingPerson(any(), eq(TEST_PERSON_ID))).thenReturn(UserResponse.builder().build());
        Advisor savedAdvisor = new Advisor();
        savedAdvisor.setIdentifier(identifier);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(savedAdvisor);
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.ADVISOR), any())).thenReturn(null);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            UUID result = advisorWriteService.createAdvisor(createRequest);

            assertNotNull(result);
            verify(personWriteService, never()).createPerson(any());
            verify(advisorRepositoryWrapper).saveWithException(any(Advisor.class));
        }
    }

    @Test
    void createAdvisor_existingPersonWithAdvisor_throwsException() {
        Person existingPerson = new Person();
        existingPerson.setId(TEST_PERSON_ID);
        when(userReadService.findUsersByPersonPhoneNumber("9876543210")).thenReturn(Collections.emptyList());
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.of(existingPerson));
        when(advisorRepositoryWrapper.findByUsername("9876543210")).thenReturn(Optional.of(advisor));

        assertThrows(BadRequestException.class, () -> advisorWriteService.createAdvisor(createRequest));
        verify(advisorRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void createAdvisor_preferredCallStartAfterEnd_throwsException() {
        createRequest.setPreferredCallStartTime(LocalTime.of(18, 0));
        createRequest.setPreferredCallEndTime(LocalTime.of(9, 0));
        when(userReadService.findUsersByPersonPhoneNumber("9876543210")).thenReturn(Collections.emptyList());
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any(PersonCreateRequest.class))).thenReturn(PersonCreateResponse.builder().id(TEST_PERSON_ID).build());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            assertThrows(BadRequestException.class, () -> advisorWriteService.createAdvisor(createRequest));
        }
    }

    @Test
    void createAdvisor_onlyOnePreferredCallTime_throwsException() {
        createRequest.setPreferredCallStartTime(LocalTime.of(9, 0));
        when(userReadService.findUsersByPersonPhoneNumber("9876543210")).thenReturn(Collections.emptyList());
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any(PersonCreateRequest.class))).thenReturn(PersonCreateResponse.builder().id(TEST_PERSON_ID).build());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            assertThrows(BadRequestException.class, () -> advisorWriteService.createAdvisor(createRequest));
        }
    }

    @Test
    void createAdvisor_withOfficeKey_validatesOffice() {
        createRequest.setOfficeKey("OFF1");
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any(PersonCreateRequest.class))).thenReturn(PersonCreateResponse.builder().id(TEST_PERSON_ID).build());
        when(officeReadService.getOfficeByKey("OFF1")).thenReturn(new com.nivasafinance.features.offices.dto.OfficeResponse(1L, "Office", "OFF1", "O1", null, null));
        Advisor savedAdvisor = new Advisor();
        savedAdvisor.setIdentifier(identifier);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(savedAdvisor);
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.ADVISOR), any())).thenReturn(null);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorWriteService.createAdvisor(createRequest);

            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertEquals("OFF1", captor.getValue().getOfficeKey());
        }
    }

    @Test
    void updateAdvisor_success() {
        UpdateAdvisorRequest request = new UpdateAdvisorRequest();
        request.setOfficeKey("OFF1");
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateAdvisor(identifier, request);

            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertEquals("OFF1", captor.getValue().getOfficeKey());
            verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
        }
    }

    @Test
    void updateAdvisor_preferredCallStartAfterEnd_throwsException() {
        UpdateAdvisorRequest request = new UpdateAdvisorRequest();
        request.setPreferredCallStartTime(LocalTime.of(18, 0));
        request.setPreferredCallEndTime(LocalTime.of(9, 0));
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);

        assertThrows(BadRequestException.class, () -> advisorWriteService.updateAdvisor(identifier, request));
    }

    @Test
    void updateSourcingDetails_existingSourceChannel_updates() {
        advisor.setSourceChannelId(10L);
        UpdateSourcingDetailsRequest request = UpdateSourcingDetailsRequest.builder()
                .sourcingChannel("CH")
                .marketingSource("MS")
                .build();
        SourcingChannelResponse channelResponse = new SourcingChannelResponse(10L, UUID.randomUUID(), null, null, null);

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(sourcingChannelWriteService.update(eq(10L), any())).thenReturn(channelResponse);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateSourcingDetails(identifier, request);

            verify(sourcingChannelWriteService).update(eq(10L), any());
            verify(advisorRepositoryWrapper).saveWithException(advisor);
        }
    }

    @Test
    void updateSourcingDetails_noSourceChannel_creates() {
        UpdateSourcingDetailsRequest request = UpdateSourcingDetailsRequest.builder()
                .sourcingChannel("CH")
                .build();
        SourcingChannelResponse channelResponse = new SourcingChannelResponse(11L, UUID.randomUUID(), null, null, null);

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(sourcingChannelWriteService.create(any())).thenReturn(channelResponse);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateSourcingDetails(identifier, request);

            verify(sourcingChannelWriteService).create(any());
            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertEquals(11L, captor.getValue().getSourceChannelId());
        }
    }

    @Test
    void updateQualificationDetails_validKey_success() {
        UpdateQualificationDetailsRequest request = new UpdateQualificationDetailsRequest();
        request.setHighestQualification("HIGH_SCHOOL");
        CodeValueResponse cv = new CodeValueResponse();
        cv.setKey("HIGH_SCHOOL");
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(codeMasterService.getAllCodeValuesByCodeKey(any(), eq(true))).thenReturn(List.of(cv));
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateQualificationDetails(identifier, request);

            verify(advisorRepositoryWrapper).saveWithException(any(Advisor.class));
        }
    }

    @Test
    void updateQualificationDetails_invalidKey_throwsException() {
        UpdateQualificationDetailsRequest request = new UpdateQualificationDetailsRequest();
        request.setHighestQualification("INVALID");
        CodeValueResponse cv = new CodeValueResponse();
        cv.setKey("OTHER");
        cv.setValue("Other");
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(codeMasterService.getAllCodeValuesByCodeKey(any(), eq(true))).thenReturn(List.of(cv));

        assertThrows(ResponseStatusException.class, () -> advisorWriteService.updateQualificationDetails(identifier, request));
    }

    @Test
    void updateOccupationDetails_validKeys_success() {
        UpdateOccupationDetailsRequest request = new UpdateOccupationDetailsRequest();
        request.setOccupationType("SALARIED");
        request.setOccupation("ENGINEER");
        CodeValueResponse cv1 = new CodeValueResponse();
        cv1.setKey("SALARIED");
        CodeValueResponse cv2 = new CodeValueResponse();
        cv2.setKey("ENGINEER");
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(codeMasterService.getAllCodeValuesByCodeKey(any(), eq(true)))
                .thenReturn(List.of(cv1))
                .thenReturn(List.of(cv2));
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateOccupationDetails(identifier, request);

            verify(advisorRepositoryWrapper).saveWithException(any(Advisor.class));
        }
    }

    @Test
    void updateSegmentationDetails_validKey_success() {
        UpdateSegmentationDetailsRequest request = new UpdateSegmentationDetailsRequest();
        request.setSegmentation("SEG1");
        CodeValueResponse cv = new CodeValueResponse();
        cv.setKey("SEG1");
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(codeMasterService.getAllCodeValuesByCodeKey(any(), eq(true))).thenReturn(List.of(cv));
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateSegmentationDetails(identifier, request);

            verify(advisorRepositoryWrapper).saveWithException(any(Advisor.class));
        }
    }

    @Test
    void rejectAdvisor_success() {
        RejectAdvisorRequest request = RejectAdvisorRequest.builder().rejected("Reason").build();
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorWriteService.rejectAdvisor(identifier, request);

            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertEquals(AdvisorStatus.REJECTED, captor.getValue().getStatus());
            assertNotNull(captor.getValue().getRejectionDetails());
            verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
        }
    }

    @Test
    void dormantAdvisor_success() {
        DormantAdvisorRequest request = new DormantAdvisorRequest();
        request.setDormant("Dormant reason");
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorWriteService.dormantAdvisor(identifier, request);

            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertEquals(AdvisorStatus.DORMANT, captor.getValue().getStatus());
        }
    }

    @Test
    void activateAdvisor_success() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorWriteService.activateAdvisor(identifier);

            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertEquals(AdvisorStatus.ACTIVE, captor.getValue().getStatus());
            verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
        }
    }

    @Test
    void outOfGeoAdvisor_success() {
        OutOfGeoAdvisorRequest request = new OutOfGeoAdvisorRequest();
        request.setOutOfGeo("Out of geo reason");
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorWriteService.outOfGeoAdvisor(identifier, request);

            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertEquals(AdvisorStatus.OUT_OF_GEO, captor.getValue().getStatus());
        }
    }

    @Test
    void updateAdvisor_onlyOnePreferredCallTime_clearsBoth() {
        advisor.setOtherDetails(new OtherDetails());
        UpdateAdvisorRequest request = new UpdateAdvisorRequest();
        request.setPreferredCallStartTime(LocalTime.of(9, 0));

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateAdvisor(identifier, request);

            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertNull(captor.getValue().getOtherDetails().getPreferredCallStartTime());
            assertNull(captor.getValue().getOtherDetails().getPreferredCallEndTime());
        }
    }

    @Test
    void updateSourcingDetails_responseNullId_doesNotUpdateSourceChannelId() {
        UpdateSourcingDetailsRequest request = UpdateSourcingDetailsRequest.builder()
                .sourcingChannel("CH")
                .build();
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(sourcingChannelWriteService.create(any())).thenReturn(new SourcingChannelResponse(null, UUID.randomUUID(), null, null, null));

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateSourcingDetails(identifier, request);

            verify(advisorRepositoryWrapper).saveWithException(advisor);
            assertNull(advisor.getSourceChannelId());
        }
    }

    @Test
    void rejectAdvisor_nullRequest_success() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorWriteService.rejectAdvisor(identifier, null);

            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertEquals(AdvisorStatus.REJECTED, captor.getValue().getStatus());
        }
    }

    @Test
    void dormantAdvisor_nullRequest_success() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorWriteService.dormantAdvisor(identifier, null);

            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertEquals(AdvisorStatus.DORMANT, captor.getValue().getStatus());
        }
    }

    @Test
    void outOfGeoAdvisor_nullRequest_success() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorWriteService.outOfGeoAdvisor(identifier, null);

            ArgumentCaptor<Advisor> captor = ArgumentCaptor.forClass(Advisor.class);
            verify(advisorRepositoryWrapper).saveWithException(captor.capture());
            assertEquals(AdvisorStatus.OUT_OF_GEO, captor.getValue().getStatus());
        }
    }

    @Test
    void updateQualificationDetails_nullHighestQualification_skipsValidation() {
        UpdateQualificationDetailsRequest request = new UpdateQualificationDetailsRequest();
        request.setHighestQualification(null);
        advisor.setQualificationDetails(null);

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateQualificationDetails(identifier, request);

            verify(codeMasterService, never()).getAllCodeValuesByCodeKey(any(), any());
            verify(advisorRepositoryWrapper).saveWithException(any(Advisor.class));
        }
    }

    @Test
    void updateOccupationDetails_invalidOccupationType_throwsException() {
        UpdateOccupationDetailsRequest request = new UpdateOccupationDetailsRequest();
        request.setOccupationType("INVALID_TYPE");
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(codeMasterService.getAllCodeValuesByCodeKey(any(), eq(true))).thenReturn(List.of(new CodeValueResponse()));

        assertThrows(ResponseStatusException.class,
                () -> advisorWriteService.updateOccupationDetails(identifier, request));
    }

    @Test
    void updateSegmentationDetails_invalidSegmentation_throwsException() {
        UpdateSegmentationDetailsRequest request = new UpdateSegmentationDetailsRequest();
        request.setSegmentation("INVALID");
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(codeMasterService.getAllCodeValuesByCodeKey(any(), eq(true))).thenReturn(List.of(new CodeValueResponse()));

        assertThrows(ResponseStatusException.class,
                () -> advisorWriteService.updateSegmentationDetails(identifier, request));
    }

    @Test
    void createAdvisor_withSourcingChannelRequest_handlesSourcingChannel() {
        createRequest.setSourcingChannelRequest(
                new com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest("CH", "MS", null));
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any(PersonCreateRequest.class))).thenReturn(PersonCreateResponse.builder().id(TEST_PERSON_ID).build());
        Advisor savedAdvisor = new Advisor();
        savedAdvisor.setId(1L);
        savedAdvisor.setIdentifier(identifier);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(savedAdvisor);
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.ADVISOR), any())).thenReturn(null);
        SourcingChannelResponse channelResponse = new SourcingChannelResponse(5L, UUID.randomUUID(), null, null, null);
        when(sourcingChannelWriteService.create(any())).thenReturn(channelResponse);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorWriteService.createAdvisor(createRequest);

            verify(sourcingChannelWriteService).create(any());
            verify(advisorRepositoryWrapper, atLeast(2)).saveWithException(any(Advisor.class));
        }
    }

    @Test
    void updateAdvisor_withMobileNumberDetails_mapsAndUpdatesPerson() {
        MobileNumberDetails mobileDetails = new MobileNumberDetails();
        mobileDetails.setMobileNumber("9999888877");
        mobileDetails.setIsPrimary(true);
        mobileDetails.setIsWhatsappAvailable(false);
        UpdateAdvisorRequest request = new UpdateAdvisorRequest();
        request.setMobileNumberDetails(List.of(mobileDetails));

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateAdvisor(identifier, request);

            ArgumentCaptor<PersonUpdateRequest> captor = ArgumentCaptor.forClass(PersonUpdateRequest.class);
            verify(personWriteService).updatePerson(eq(TEST_PERSON_ID), captor.capture());
            assertNotNull(captor.getValue().getMobileNumbers());
            assertEquals(1, captor.getValue().getMobileNumbers().size());
            assertEquals("9999888877", captor.getValue().getMobileNumbers().get(0).getNumber());
        }
    }

    @Test
    void updateAdvisor_personWithPrimaryMobile_publishesEventWithMobile() {
        Person personWithMobile = new Person();
        com.nivasafinance.features.person.entity.MobileNumberDetails primary =
                new com.nivasafinance.features.person.entity.MobileNumberDetails("8888777766", true, false);
        personWithMobile.setMobileNumbers(List.of(primary));

        UpdateAdvisorRequest request = new UpdateAdvisorRequest();
        request.setOfficeKey("OFF1");
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(personWithMobile);

            advisorWriteService.updateAdvisor(identifier, request);

            ArgumentCaptor<SystemEvent> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
            verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
            Object payload = eventCaptor.getValue().getPayload();
            assertTrue(payload instanceof com.nivasafinance.common.events.payload.AdvisorUpdateEventPayload);
            assertEquals("8888777766", ((com.nivasafinance.common.events.payload.AdvisorUpdateEventPayload) payload).getMobileNumber());
        }
    }

    @Test
    void updateAdvisor_withPersonalDetails_mapsToPersonUpdateRequest() {
        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setFirstName("Jane");
        personalDetails.setLastName("Doe");
        UpdateAdvisorRequest request = new UpdateAdvisorRequest();
        request.setPersonalDetails(personalDetails);

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

            advisorWriteService.updateAdvisor(identifier, request);

            ArgumentCaptor<PersonUpdateRequest> captor = ArgumentCaptor.forClass(PersonUpdateRequest.class);
            verify(personWriteService).updatePerson(eq(TEST_PERSON_ID), captor.capture());
            assertEquals("Jane", captor.getValue().getFirstName());
            assertEquals("Doe", captor.getValue().getLastName());
        }
    }
}
