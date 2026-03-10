package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.advisor.dto.AdvisorUpdateCallLog;
import com.nivasafinance.features.advisor.dto.CreateAdvisorCallRequest;
import com.nivasafinance.features.advisor.dto.CreateExternalCallLogRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.dto.CreateCallLogResponse;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.service.CallWriteService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorCallWriteServiceImplTest {

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private PersonReadService personReadService;

    @Mock
    private CallWriteService callWriteService;

    @Mock
    private CallReadService callReadService;

    @Mock
    private UserReadService userReadService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AdvisorCallWriteServiceImpl advisorCallWriteService;

    private static final Long TEST_PERSON_ID = 2L;

    private UUID advisorIdentifier;
    private Advisor advisor;
    private CreateAdvisorCallRequest createRequest;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(advisorIdentifier);
        advisor.setPersonId(TEST_PERSON_ID);
        advisor.setCallLogDetails(null);
        advisor.setOtherDetails(null);

        createRequest = new CreateAdvisorCallRequest();
        createRequest.setPhoneNumber("9876543210");
    }

    @Test
    void callPerson_success_returnsResponse() {
        PersonResponse person = new PersonResponse();
        MobileNumberDetails mobile = new MobileNumberDetails();
        mobile.setNumber("9876543210");
        mobile.setIsPrimary(true);
        person.setMobileNumbers(List.of(mobile));
        person.setDisplayName("Test Person");

        com.nivasafinance.features.usermanagement.dto.UserResponse userResponse = new com.nivasafinance.features.usermanagement.dto.UserResponse();
        PersonResponse currentUserPerson = new PersonResponse();
        currentUserPerson.setMobileNumbers(List.of(mobile));
        userResponse.setPersonResponse(currentUserPerson);

        UUID callIdentifier = UUID.randomUUID();
        InitiateCallResponse initiateResponse = InitiateCallResponse.builder()
                .id(100L)
                .identifier(callIdentifier)
                .status(CallStatus.QUEUED)
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(person);
        when(userReadService.getUserByUsername(any())).thenReturn(userResponse);
        when(callWriteService.call(any())).thenReturn(initiateResponse);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            var result = advisorCallWriteService.callPerson(advisorIdentifier, createRequest);

            assertNotNull(result);
            assertEquals(callIdentifier, result.getIdentifier());
            assertEquals(CallStatus.QUEUED, result.getStatus());
            verify(callWriteService).call(any());
            verify(advisorRepositoryWrapper).saveWithException(advisor);
        }
    }

    @Test
    void callPerson_phoneNotBelongToPerson_throwsException() {
        PersonResponse person = new PersonResponse();
        person.setMobileNumbers(List.of());
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(person);

        assertThrows(com.nivasafinance.common.exception.BadRequestException.class,
                () -> advisorCallWriteService.callPerson(advisorIdentifier, createRequest));
        verify(callWriteService, never()).call(any());
    }

    @Test
    void updateCallLog_success() {
        String externalId = "ext-123";
        AdvisorUpdateCallLog request = new AdvisorUpdateCallLog();
        request.setStatus(CallStatus.COMPLETED);

        CallLogResponse updatedCallLog = CallLogResponse.builder()
                .id(100L)
                .identifier(UUID.randomUUID())
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedCallLog));

        advisorCallWriteService.updateCallLog(advisorIdentifier, externalId, request);

        verify(callWriteService).updateCallLogByProviderId(eq(externalId), any());
        verify(callReadService).getCallLogByProviderId(externalId);
    }

    @Test
    void createExternalCallLog_success_returnsResponse() {
        PersonResponse person = new PersonResponse();
        MobileNumberDetails mobile = new MobileNumberDetails();
        mobile.setNumber("9876543210");
        person.setMobileNumbers(List.of(mobile));

        CreateExternalCallLogRequest request = CreateExternalCallLogRequest.builder()
                .provider(CallProvider.EXOTEL)
                .providerId("pid-1")
                .fromNumber("1111111111")
                .toNumber("9876543210")
                .direction(CallDirection.OUTBOUND)
                .status(CallStatus.COMPLETED)
                .build();

        UUID callIdentifier = UUID.randomUUID();
        CreateCallLogResponse savedCallLog = CreateCallLogResponse.builder()
                .id(100L)
                .identifier(callIdentifier)
                .status(CallStatus.COMPLETED)
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(personReadService.getPersonById(TEST_PERSON_ID)).thenReturn(person);
        when(callWriteService.createCallLog(any())).thenReturn(savedCallLog);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            var result = advisorCallWriteService.createExternalCallLog(advisorIdentifier, request);

            assertNotNull(result);
            assertEquals(callIdentifier, result.getIdentifier());
            assertEquals(CallStatus.COMPLETED, result.getStatus());
            verify(callWriteService).createCallLog(any());
            verify(advisorRepositoryWrapper).saveWithException(advisor);
        }
    }
}
