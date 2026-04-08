package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.enums.SystemEntities;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.call.dto.InitiateCallRequest;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.entity.CallLogLead;
import com.nivasafinance.features.call.entity.RoleCallConfigs;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.repository.CallLogLeadRepositoryWrapper;
import com.nivasafinance.features.call.repository.CallLogRepositoryWrapper;
import com.nivasafinance.features.call.repository.RoleCallConfigsRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.voice.VoiceHandler;
import com.nivasafinance.services.voice.dto.VoiceCallRequest;
import com.nivasafinance.services.voice.dto.VoiceCallResponse;
import com.nivasafinance.services.voice.dto.VoiceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallWriteServiceImplTest {

    @Mock
    private CallLogRepositoryWrapper callLogRepositoryWrapper;
    @Mock
    private CallLogLeadRepositoryWrapper callLogLeadRepositoryWrapper;
    @Mock
    private CallReadService callReadService;
    @Mock
    private RoleCallConfigsRepositoryWrapper roleCallConfigsRepositoryWrapper;
    @Mock
    private UserRoleService userRoleService;
    @Mock
    private ServiceFactory<VoiceHandler> thirdPartyserviceFactory;

    @InjectMocks
    private CallWriteServiceImpl service;

    @Test
    void call_success_savesCallLogAndMapsLead() {
        InitiateCallRequest request = InitiateCallRequest.builder()
                .fromPhoneNumber("111")
                .toPhoneNumber("222")
                .entity(SystemEntities.LEAD)
                .entityId(50L)
                .identifier("lead-identifier")
                .contactId(70L)
                .businessPurpose("test")
                .build();

        RoleCallConfigs configs = new RoleCallConfigs();
        configs.setCallerId("caller-1");

        VoiceHandler handler = mock(VoiceHandler.class);
        VoiceCallResponse voiceResponse = mock(VoiceCallResponse.class);
        when(voiceResponse.getCallId()).thenReturn("call-id-1");
        when(voiceResponse.getStatus()).thenReturn(VoiceStatus.COMPLETED);

        CallLog savedCallLog = new CallLog();
        savedCallLog.setId(100L);
        savedCallLog.setIdentifier(UUID.randomUUID());
        savedCallLog.setStatus(CallStatus.COMPLETED);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("alice");
            when(userRoleService.getPrimaryRoleForUsername("alice")).thenReturn("ROLE");
            when(roleCallConfigsRepositoryWrapper.findByRoleWithException("ROLE")).thenReturn(configs);
            when(thirdPartyserviceFactory.getHandler(ThirdPartyServiceList.VOICE)).thenReturn(handler);
            when(handler.makeCall(any(VoiceCallRequest.class), any(BusinessContext.class))).thenReturn(voiceResponse);
            when(callLogRepositoryWrapper.saveWithException(any(CallLog.class))).thenAnswer(invocation -> {
                CallLog log = invocation.getArgument(0);
                log.setId(savedCallLog.getId());
                log.setIdentifier(savedCallLog.getIdentifier());
                log.setStatus(savedCallLog.getStatus());
                return log;
            });
            when(callLogRepositoryWrapper.findByIdWithException(100L)).thenReturn(savedCallLog);
            when(callLogLeadRepositoryWrapper.findByCallLogId(100L)).thenReturn(Optional.empty());

            InitiateCallResponse response = service.call(request);

            assertEquals(100L, response.getId());
            assertEquals(savedCallLog.getIdentifier(), response.getIdentifier());
            verify(handler).makeCall(any(VoiceCallRequest.class), any(BusinessContext.class));
            verify(callLogLeadRepositoryWrapper).saveWithException(any(CallLogLead.class));
        }
    }

    @Test
    void call_missingUsername_throwsIllegalStateException() {
        InitiateCallRequest request = new InitiateCallRequest();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn(null);

            assertThrows(IllegalStateException.class, () -> service.call(request));
        }
    }

    @Test
    void call_missingPrimaryRole_throwsIllegalStateException() {
        InitiateCallRequest request = new InitiateCallRequest();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("alice");
            when(userRoleService.getPrimaryRoleForUsername("alice")).thenReturn("");

            assertThrows(IllegalStateException.class, () -> service.call(request));
        }
    }

    @Test
    void updateCallLogByProviderId_notFound_throwsBadRequest() {
        when(callReadService.getCallLogByProviderId("missing")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> service.updateCallLogByProviderId("missing", new UpdateCallLog()));
    }

    @Test
    void updateCallLogByProviderId_updatesFields() {
        CallLog existing = new CallLog();
        existing.setProviderId("pid");
        UpdateCallLog update = UpdateCallLog.builder()
                .status(CallStatus.COMPLETED)
                .recordingDetails(CallLog.RecordingDetails.builder().url("url").build())
                .completionDetails(CallLog.CompletionDetails.builder().build())
                .build();
        when(callReadService.getCallLogByProviderId("pid"))
                .thenReturn(Optional.of(new com.nivasafinance.features.call.dto.CallLogResponse()));
        when(callLogRepositoryWrapper.findByProviderIdWithException("pid")).thenReturn(existing);

        service.updateCallLogByProviderId("pid", update);

        verify(callLogRepositoryWrapper).saveWithException(existing);
        assertEquals(CallStatus.COMPLETED, existing.getStatus());
        assertEquals("url", existing.getRecordingDetails().getUrl());
    }

    @Test
    void createCallLog_duplicateProviderId_throwsBadRequest() {
        CallLog existing = new CallLog();
        existing.setProviderId("pid");
        when(callReadService.getCallLogByProviderId("pid"))
                .thenReturn(Optional.of(new com.nivasafinance.features.call.dto.CallLogResponse()));

        CallLog newCallLog = new CallLog();
        newCallLog.setProviderId("pid");

        assertThrows(BadRequestException.class, () -> service.createCallLog(newCallLog));
    }

    @Test
    void createCallLog_success_returnsResponse() {
        CallLog newCallLog = new CallLog();
        newCallLog.setProviderId("pid-new");
        CallLog saved = new CallLog();
        saved.setId(15L);
        saved.setIdentifier(UUID.randomUUID());
        saved.setStatus(CallStatus.QUEUED);

        when(callReadService.getCallLogByProviderId("pid-new")).thenReturn(Optional.empty());
        when(callLogRepositoryWrapper.saveWithException(newCallLog)).thenReturn(saved);

        var response = service.createCallLog(newCallLog);

        assertEquals(15L, response.getId());
        assertEquals(saved.getIdentifier(), response.getIdentifier());
        assertEquals(CallStatus.QUEUED, response.getStatus());
    }

    @Test
    void mapCallLogToLead_missingIds_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.mapCallLogToLead(null, 1L, null));
        assertThrows(BadRequestException.class, () -> service.mapCallLogToLead(1L, null, null));
    }

    @Test
    void mapCallLogToLead_existingDifferentLead_throwsBadRequest() {
        CallLogLead mapping = new CallLogLead(5L, 99L, null);
        when(callLogRepositoryWrapper.findByIdWithException(5L)).thenReturn(new CallLog());
        when(callLogLeadRepositoryWrapper.findByCallLogId(5L)).thenReturn(Optional.of(mapping));

        assertThrows(BadRequestException.class, () -> service.mapCallLogToLead(5L, 1L, null));
    }

    @Test
    void mapCallLogToLead_savesWhenAbsent() {
        when(callLogRepositoryWrapper.findByIdWithException(5L)).thenReturn(new CallLog());
        when(callLogLeadRepositoryWrapper.findByCallLogId(5L)).thenReturn(Optional.empty());

        service.mapCallLogToLead(5L, 10L, 20L);

        verify(callLogLeadRepositoryWrapper).saveWithException(any(CallLogLead.class));
    }

    @Test
    void mergeAiAnalysisByIdentifier_mergesFields() {
        CallLog.AiAnalysisDetails existing = CallLog.AiAnalysisDetails.builder()
                .jobId("job-1")
                .status(null)
                .summaryUrl("old")
                .build();
        CallLog callLog = new CallLog();
        callLog.setAiAnalysis(existing);
        when(callLogRepositoryWrapper.findByIdentifierWithException(any())).thenReturn(callLog);

        CallLog.AiAnalysisDetails patch = CallLog.AiAnalysisDetails.builder()
                .status(com.nivasafinance.features.call.enums.AtlasJobStatus.COMPLETED)
                .error("err")
                .build();

        service.mergeAiAnalysisByIdentifier(UUID.randomUUID(), patch);

        ArgumentCaptor<CallLog> captor = ArgumentCaptor.forClass(CallLog.class);
        verify(callLogRepositoryWrapper).saveWithException(captor.capture());
        CallLog saved = captor.getValue();
        assertEquals("job-1", saved.getAiAnalysis().getJobId());
        assertEquals(com.nivasafinance.features.call.enums.AtlasJobStatus.COMPLETED, saved.getAiAnalysis().getStatus());
        assertEquals("err", saved.getAiAnalysis().getError());
        assertEquals("old", saved.getAiAnalysis().getSummaryUrl());
    }
}
