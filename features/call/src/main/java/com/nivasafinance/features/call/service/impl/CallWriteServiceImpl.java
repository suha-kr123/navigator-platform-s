package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.call.dto.InitiateCallRequest;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.entity.RoleCallConfigs;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.enums.CallSource;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.repository.CallLogRepositoryWrapper;
import com.nivasafinance.features.call.repository.RoleCallConfigsRepositoryWrapper;
import com.nivasafinance.features.call.service.CallWriteService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.voice.VoiceHandler;
import com.nivasafinance.services.voice.dto.VoiceCallRequest;
import com.nivasafinance.services.voice.dto.VoiceCallResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CallWriteServiceImpl implements CallWriteService {

    private final CallLogRepositoryWrapper callLogRepositoryWrapper;
    private final RoleCallConfigsRepositoryWrapper roleCallConfigsRepositoryWrapper;
    private final UserRoleService userRoleService;
    private final ServiceFactory<VoiceHandler> thirdPartyserviceFactory;

    @Override
    public InitiateCallResponse call(InitiateCallRequest request) {
        String username = UserContext.getUsername();
        if (!StringUtils.hasText(username)) {
            throw new IllegalStateException("No authenticated user found in context");
        }

        String primaryRole = userRoleService.getPrimaryRoleForUsername(username);
        if (primaryRole == null || primaryRole.isBlank()) {
            throw new IllegalStateException("Primary Role not found for user : " + username);
        }
        RoleCallConfigs roleCallConfigs = roleCallConfigsRepositoryWrapper.findByRoleWithException(primaryRole);
        VoiceHandler handler = thirdPartyserviceFactory.getHandler(ThirdPartyServiceList.VOICE);
        VoiceCallResponse voiceCallResponse = handler.makeCall(
                buildRequest(request, roleCallConfigs),
                new BusinessContext(request.getEntity().name(), request.getEntityId(), request.getBusinessPurpose()));

        CallLog callLog = buildCallLog(request, roleCallConfigs, voiceCallResponse);
        CallLog savedCallLog = callLogRepositoryWrapper.saveWithException(callLog);

        return InitiateCallResponse.builder()
                .id(savedCallLog.getId())
                .identifier(savedCallLog.getIdentifier())
                .status(savedCallLog.getStatus())
                .build();
    }

    @Override
    public void updateCallLogByProviderId(String providerId, UpdateCallLog updateCallLog) {
        CallLog callLog = callLogRepositoryWrapper.findByProviderIdWithException(providerId);
        callLog.setStatus(updateCallLog.getStatus());
        callLog.setRecordingDetails(updateCallLog.getRecordingDetails());
        callLog.setCompletionDetails(updateCallLog.getCompletionDetails());
        callLogRepositoryWrapper.saveWithException(callLog);
    }

    private CallLog buildCallLog(InitiateCallRequest request, RoleCallConfigs roleCallConfigs, VoiceCallResponse voiceCallResponse) {
        CallLog callLog = new CallLog();
        callLog.setProvider(CallProvider.EXOTEL);
        callLog.setProviderId(voiceCallResponse.getCallId());
        callLog.setCallerId(roleCallConfigs.getCallerId());
        callLog.setFromNumber(request.getFromPhoneNumber());
        callLog.setToNumber(request.getToPhoneNumber());
        callLog.setDirection(CallDirection.OUTBOUND);
        callLog.setSource(CallSource.CRM);
        callLog.setStatus(CallStatus.fromVoiceStatus(voiceCallResponse.getStatus()));
        return callLog;
    }

    private VoiceCallRequest buildRequest(InitiateCallRequest request, RoleCallConfigs roleCallConfigs) {
        return new VoiceCallRequest(request.getFromPhoneNumber(), request.getToPhoneNumber(),
                roleCallConfigs.getCallerId(),
                new VoiceCallRequest.CallBackData(request.getEntity(), request.getIdentifier()));
    }
}

