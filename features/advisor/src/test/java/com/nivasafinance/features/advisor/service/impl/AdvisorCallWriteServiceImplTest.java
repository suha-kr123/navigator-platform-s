package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.advisor.dto.AdvisorUpdateCallLog;
import com.nivasafinance.features.advisor.dto.CreateAdvisorCallRequest;
import com.nivasafinance.features.advisor.dto.CreateExternalCallLogRequest;
import com.nivasafinance.features.advisor.dto.OtherDetails;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.dto.CreateCallLogResponse;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.service.CallWriteService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorCallWriteServiceImplTest {

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

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

    private static final String ADVISOR_USERNAME = "adv_user";

    private UUID advisorIdentifier;
    private Advisor advisor;
    private CreateAdvisorCallRequest createRequest;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(advisorIdentifier);
        advisor.setUsername(ADVISOR_USERNAME);
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

        UserResponse userResponse = new UserResponse();
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
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(person);
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
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(person);

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
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(person);
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

    @Test
    void callPerson_existingCallLogDetails_appendsToList() {
        advisor.setCallLogDetails(new ArrayList<>(List.of(new Advisor.CallLogDetails(1L))));
        PersonResponse person = new PersonResponse();
        MobileNumberDetails mobile = new MobileNumberDetails();
        mobile.setNumber("9876543210");
        mobile.setIsPrimary(true);
        person.setMobileNumbers(List.of(mobile));

        UserResponse userResponse = new UserResponse();
        PersonResponse currentUserPerson = new PersonResponse();
        currentUserPerson.setMobileNumbers(List.of(mobile));
        userResponse.setPersonResponse(currentUserPerson);

        InitiateCallResponse initiateResponse = InitiateCallResponse.builder()
                .id(200L)
                .identifier(UUID.randomUUID())
                .status(CallStatus.QUEUED)
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(person);
        when(userReadService.getUserByUsername(any())).thenReturn(userResponse);
        when(callWriteService.call(any())).thenReturn(initiateResponse);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            advisorCallWriteService.callPerson(advisorIdentifier, createRequest);
            assertEquals(2, advisor.getCallLogDetails().size());
        }
    }

    @Test
    void callPerson_currentUserWithoutPrimaryMobile_throwsBadRequest() {
        PersonResponse person = new PersonResponse();
        MobileNumberDetails mobile = new MobileNumberDetails();
        mobile.setNumber("9876543210");
        mobile.setIsPrimary(true);
        person.setMobileNumbers(List.of(mobile));

        UserResponse userResponse = new UserResponse();
        PersonResponse currentUserPerson = new PersonResponse();
        MobileNumberDetails nonPrimary = new MobileNumberDetails();
        nonPrimary.setNumber("1111111111");
        nonPrimary.setIsPrimary(false);
        currentUserPerson.setMobileNumbers(List.of(nonPrimary));
        userResponse.setPersonResponse(currentUserPerson);

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(person);
        when(userReadService.getUserByUsername(any())).thenReturn(userResponse);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            assertThrows(BadRequestException.class,
                    () -> advisorCallWriteService.callPerson(advisorIdentifier, createRequest));
        }
        verify(callWriteService, never()).call(any());
    }

    @Test
    void updateCallLog_withRecordingCompletionAndLegs_updatesProvider() {
        String externalId = "ext-456";
        AdvisorUpdateCallLog request = new AdvisorUpdateCallLog();
        request.setStatus(CallStatus.COMPLETED);
        request.setRecordingDetails(AdvisorUpdateCallLog.RecordingDetails.builder().url("https://rec").build());
        AdvisorUpdateCallLog.CompletionLeg leg = AdvisorUpdateCallLog.CompletionLeg.builder()
                .duration("10")
                .direction("OUTBOUND")
                .status(CallStatus.COMPLETED)
                .build();
        request.setCompletionDetails(AdvisorUpdateCallLog.CompletionDetails.builder()
                .duration(10L)
                .startTime(LocalDateTime.now().minusMinutes(1))
                .endTime(LocalDateTime.now())
                .legs(List.of(leg))
                .build());

        CallLogResponse updatedCallLog = CallLogResponse.builder()
                .id(200L)
                .identifier(UUID.randomUUID())
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedCallLog));

        advisorCallWriteService.updateCallLog(advisorIdentifier, externalId, request);

        verify(callWriteService).updateCallLogByProviderId(eq(externalId), any());
    }

    @Test
    void updateCallLog_callNotFoundAfterUpdate_throwsBadRequest() {
        String externalId = "ext-missing";
        AdvisorUpdateCallLog request = new AdvisorUpdateCallLog();
        request.setStatus(CallStatus.COMPLETED);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> advisorCallWriteService.updateCallLog(advisorIdentifier, externalId, request));
    }

    @Test
    void createExternalCallLog_withCreatedAt_passesThroughToCallWrite() {
        PersonResponse person = new PersonResponse();
        MobileNumberDetails mobile = new MobileNumberDetails();
        mobile.setNumber("9876543210");
        person.setMobileNumbers(List.of(mobile));
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);

        CreateExternalCallLogRequest request = CreateExternalCallLogRequest.builder()
                .provider(CallProvider.EXOTEL)
                .providerId("pid-2")
                .fromNumber("1111111111")
                .toNumber("9876543210")
                .direction(CallDirection.OUTBOUND)
                .status(CallStatus.COMPLETED)
                .createdAt(createdAt)
                .recordingDetails(CallLog.RecordingDetails.builder().url("u").build())
                .completionDetails(CallLog.CompletionDetails.builder().duration(1L).build())
                .build();

        UUID callIdentifier = UUID.randomUUID();
        CreateCallLogResponse savedCallLog = CreateCallLogResponse.builder()
                .id(300L)
                .identifier(callIdentifier)
                .status(CallStatus.COMPLETED)
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(person);
        when(callWriteService.createCallLog(any())).thenReturn(savedCallLog);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            advisorCallWriteService.createExternalCallLog(advisorIdentifier, request);
        }

        verify(callWriteService).createCallLog(argThat(cl ->
                createdAt.equals(cl.getCreatedAt())
                        && cl.getRecordingDetails() != null
                        && cl.getCompletionDetails() != null));
    }

    @Test
    void updateLastCallId_whenNewCallIsNewer_updatesLastCallId() {
        OtherDetails od = new OtherDetails();
        od.setLastCallId(50L);
        advisor.setOtherDetails(od);

        PersonResponse person = new PersonResponse();
        MobileNumberDetails mobile = new MobileNumberDetails();
        mobile.setNumber("9876543210");
        mobile.setIsPrimary(true);
        person.setMobileNumbers(List.of(mobile));

        UserResponse userResponse = new UserResponse();
        PersonResponse currentUserPerson = new PersonResponse();
        currentUserPerson.setMobileNumbers(List.of(mobile));
        userResponse.setPersonResponse(currentUserPerson);

        LocalDateTime older = LocalDateTime.of(2024, 6, 1, 10, 0);
        LocalDateTime newer = LocalDateTime.of(2024, 6, 2, 10, 0);
        when(callReadService.getCallLogByID(50L)).thenReturn(
                CallLogResponse.builder().id(50L).createdAt(older).build());
        when(callReadService.getCallLogByID(100L)).thenReturn(
                CallLogResponse.builder().id(100L).createdAt(newer).build());

        InitiateCallResponse initiateResponse = InitiateCallResponse.builder()
                .id(100L)
                .identifier(UUID.randomUUID())
                .status(CallStatus.QUEUED)
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(person);
        when(userReadService.getUserByUsername(any())).thenReturn(userResponse);
        when(callWriteService.call(any())).thenReturn(initiateResponse);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            advisorCallWriteService.callPerson(advisorIdentifier, createRequest);
        }

        assertEquals(100L, advisor.getOtherDetails().getLastCallId());
    }

    @Test
    void updateLastCallId_whenNewCallIsOlder_keepsExistingLastCallId() {
        OtherDetails od = new OtherDetails();
        od.setLastCallId(50L);
        advisor.setOtherDetails(od);

        PersonResponse person = new PersonResponse();
        MobileNumberDetails mobile = new MobileNumberDetails();
        mobile.setNumber("9876543210");
        mobile.setIsPrimary(true);
        person.setMobileNumbers(List.of(mobile));

        UserResponse userResponse = new UserResponse();
        PersonResponse currentUserPerson = new PersonResponse();
        currentUserPerson.setMobileNumbers(List.of(mobile));
        userResponse.setPersonResponse(currentUserPerson);

        LocalDateTime newer = LocalDateTime.of(2024, 6, 2, 10, 0);
        LocalDateTime older = LocalDateTime.of(2024, 6, 1, 10, 0);
        when(callReadService.getCallLogByID(50L)).thenReturn(
                CallLogResponse.builder().id(50L).createdAt(newer).build());
        when(callReadService.getCallLogByID(100L)).thenReturn(
                CallLogResponse.builder().id(100L).createdAt(older).build());

        InitiateCallResponse initiateResponse = InitiateCallResponse.builder()
                .id(100L)
                .identifier(UUID.randomUUID())
                .status(CallStatus.QUEUED)
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(person);
        when(userReadService.getUserByUsername(any())).thenReturn(userResponse);
        when(callWriteService.call(any())).thenReturn(initiateResponse);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            advisorCallWriteService.callPerson(advisorIdentifier, createRequest);
        }

        assertEquals(50L, advisor.getOtherDetails().getLastCallId());
    }

    @Test
    void updateLastCallId_sameCallLogId_skipsCompare() {
        OtherDetails od = new OtherDetails();
        od.setLastCallId(100L);
        advisor.setOtherDetails(od);

        PersonResponse person = new PersonResponse();
        MobileNumberDetails mobile = new MobileNumberDetails();
        mobile.setNumber("9876543210");
        mobile.setIsPrimary(true);
        person.setMobileNumbers(List.of(mobile));

        UserResponse userResponse = new UserResponse();
        PersonResponse currentUserPerson = new PersonResponse();
        currentUserPerson.setMobileNumbers(List.of(mobile));
        userResponse.setPersonResponse(currentUserPerson);

        InitiateCallResponse initiateResponse = InitiateCallResponse.builder()
                .id(100L)
                .identifier(UUID.randomUUID())
                .status(CallStatus.QUEUED)
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(person);
        when(userReadService.getUserByUsername(any())).thenReturn(userResponse);
        when(callWriteService.call(any())).thenReturn(initiateResponse);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            advisorCallWriteService.callPerson(advisorIdentifier, createRequest);
        }

        verify(callReadService, never()).getCallLogByID(anyLong());
    }
}
