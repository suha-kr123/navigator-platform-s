package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.call.dto.CreateCallLogResponse;
import com.nivasafinance.features.call.dto.InitiateCallRequest;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.entity.RoleCallConfigs;
import com.nivasafinance.common.enums.SystemEntities;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.enums.CallSource;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.entity.CallLogLead;
import com.nivasafinance.features.call.repository.CallLogLeadRepositoryWrapper;
import com.nivasafinance.features.call.repository.CallLogRepositoryWrapper;
import com.nivasafinance.features.call.repository.RoleCallConfigsRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CallWriteServiceImpl implements CallWriteService {

    private final CallLogRepositoryWrapper callLogRepositoryWrapper;
    private final CallLogLeadRepositoryWrapper callLogLeadRepositoryWrapper;
    private final CallReadService callReadService;
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
        if (request.getEntity() == SystemEntities.LEAD && request.getEntityId() != null) {
            mapCallLogToLead(savedCallLog.getId(), request.getEntityId(), request.getContactId());
        }

        return InitiateCallResponse.builder()
                .id(savedCallLog.getId())
                .identifier(savedCallLog.getIdentifier())
                .status(savedCallLog.getStatus())
                .build();
    }

    @Override
    public void updateCallLogByProviderId(String providerId, UpdateCallLog updateCallLog) {
        callReadService.getCallLogByProviderId(providerId)
                .orElseThrow(() -> new BadRequestException("Call log with provider ID " + providerId + " does not exist"));
        
        CallLog callLog = callLogRepositoryWrapper.findByProviderIdWithException(providerId);
        callLog.setStatus(updateCallLog.getStatus());
        callLog.setRecordingDetails(updateCallLog.getRecordingDetails());
        callLog.setCompletionDetails(updateCallLog.getCompletionDetails());
        callLogRepositoryWrapper.saveWithException(callLog);
    }

    @Override
    @Transactional
    public CreateCallLogResponse createCallLog(CallLog callLog) {
        // Check if call log with same providerId already exists
        callReadService.getCallLogByProviderId(callLog.getProviderId())
                .ifPresent(existingCallLog -> {
                    throw new BadRequestException("Call log with provider ID " + callLog.getProviderId() + " already exists");
                });
        
        CallLog savedCallLog = callLogRepositoryWrapper.saveWithException(callLog);
        return CreateCallLogResponse.builder()
                .id(savedCallLog.getId())
                .identifier(savedCallLog.getIdentifier())
                .status(savedCallLog.getStatus())
                .build();
    }

    @Override
    public void mapCallLogToLead(Long callLogId, Long leadId, Long contactId) {
        if (callLogId == null || leadId == null) {
            throw new BadRequestException("callLogId and leadId are required to map call log to lead");
        }
        callLogRepositoryWrapper.findByIdWithException(callLogId);
        var existingMapping = callLogLeadRepositoryWrapper.findByCallLogId(callLogId);
        if (existingMapping.isPresent()) {
            if (!existingMapping.get().getLeadId().equals(leadId)) {
                throw new BadRequestException("Call log " + callLogId + " is already mapped to a different lead");
            }
            return;
        }
        callLogLeadRepositoryWrapper.saveWithException(new CallLogLead(callLogId, leadId, contactId));
    }

    @Override
    @Transactional
    public void mergeAiAnalysisByIdentifier(UUID callLogIdentifier, CallLog.AiAnalysisDetails patch) {
        if (patch == null) {
            return;
        }
        CallLog callLog = callLogRepositoryWrapper.findByIdentifierWithException(callLogIdentifier);
        callLog.setAiAnalysis(mergeAiAnalysis(callLog.getAiAnalysis(), patch));
        callLogRepositoryWrapper.saveWithException(callLog);
    }

    @Override
    @Transactional
    public void mergeAiAnalysisByProviderId(String providerId, CallLog.AiAnalysisDetails patch) {
        if (patch == null) {
            return;
        }
        CallLog callLog = callLogRepositoryWrapper.findByProviderIdWithException(providerId);
        callLog.setAiAnalysis(mergeAiAnalysis(callLog.getAiAnalysis(), patch));
        callLogRepositoryWrapper.saveWithException(callLog);
    }

    private static CallLog.AiAnalysisDetails mergeAiAnalysis(
            CallLog.AiAnalysisDetails existing,
            CallLog.AiAnalysisDetails patch) {
        CallLog.AiAnalysisDetails base = existing != null ? existing : CallLog.AiAnalysisDetails.builder().build();
        return CallLog.AiAnalysisDetails.builder()
                .jobId(patch.getJobId() != null ? patch.getJobId() : base.getJobId())
                .status(patch.getStatus() != null ? patch.getStatus() : base.getStatus())
                .summaryUrl(patch.getSummaryUrl() != null ? patch.getSummaryUrl() : base.getSummaryUrl())
                .analysisUrl(patch.getAnalysisUrl() != null ? patch.getAnalysisUrl() : base.getAnalysisUrl())
                .transcriptUrl(patch.getTranscriptUrl() != null ? patch.getTranscriptUrl() : base.getTranscriptUrl())
                .summary(patch.getSummary() != null ? patch.getSummary() : base.getSummary())
                .extractedData(patch.getExtractedData() != null ? patch.getExtractedData() : base.getExtractedData())
                .executionId(patch.getExecutionId() != null ? patch.getExecutionId() : base.getExecutionId())
                .agentId(patch.getAgentId() != null ? patch.getAgentId() : base.getAgentId())
                .transcriptAiTool(patch.getTranscriptAiTool() != null ? patch.getTranscriptAiTool() : base.getTranscriptAiTool())
                .error(patch.getError() != null ? patch.getError() : base.getError())
                .build();
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

