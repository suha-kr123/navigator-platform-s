package com.nivasafinance.services.voice;

import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nivasafinance.services.voice.dto.CallLogRequest;
import com.nivasafinance.services.voice.dto.VoiceCallRequest;
import com.nivasafinance.services.voice.dto.VoiceCallResponse;
import com.nivasafinance.services.voice.service.CallLogService;

@Service
public class VoiceService {

    private final ServiceFactory<VoiceHandler> serviceFactory;
    private final CallLogService callLogService;

    @Autowired
    public VoiceService(ServiceFactory<VoiceHandler> serviceFactory, CallLogService callLogService) {
        this.serviceFactory = serviceFactory;
        this.callLogService = callLogService;
    }

    public VoiceCallResponse makeCall(VoiceCallRequest request, BusinessContext businessContext) {
        VoiceHandler voiceHandler = serviceFactory.getHandler(ThirdPartyServiceList.VOICE);
        VoiceCallResponse response = voiceHandler.makeCall(request, businessContext);

        // Log the call if we have entity information
        if (request.getEntityName() != null && request.getEntityId() != null) {
            try {
                callLogService.createCallLog(
                        new CallLogRequest(
                                response.getCallSid(),
                                request.getEntityName(),
                                request.getEntityId(),
                                "outgoing",
                                response.getFromNumber(),
                                response.getToNumber(),
                                response.getStatus(),
                                request.getExophone()
                        )
                );
            } catch (Exception e) {
                // Log error but don't fail the call
                // In production, you might want to use a proper logger
                // Just log the error and continue - don't throw exception
            }
        }

        return response;
    }

    public VoiceCallResponse getCallStatus(String callSid, BusinessContext businessContext) {
        VoiceHandler voiceHandler = serviceFactory.getHandler(ThirdPartyServiceList.VOICE);
        VoiceCallResponse response = voiceHandler.getCallStatus(callSid, businessContext);

        // Update call log with current status
        if (response.getCallSid() != null && !response.getCallSid().isEmpty()) {
            try {
                callLogService.updateCallStatus(
                        response.getCallSid(),
                        response.getStatus(),
                        response.getRecordingUrl()
                );
            } catch (Exception e) {
                // Log error but don't fail the status check
                // In production, you might want to use a proper logger
                // Just log the error and continue - don't throw exception
            }
        }

        return response;
    }
}

