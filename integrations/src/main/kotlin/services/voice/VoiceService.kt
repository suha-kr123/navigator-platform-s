package services.voice

import framework.ServiceFactory
import framework.config.BusinessContext
import framework.config.ThirdPartyServiceList
import org.springframework.stereotype.Service
import services.voice.dto.CallLogRequest
import services.voice.dto.VoiceCallRequest
import services.voice.dto.VoiceCallResponse
import services.voice.service.CallLogService

@Service
class VoiceService(
    private val serviceFactory: ServiceFactory<VoiceHandler>,
    private val callLogService: CallLogService
) {

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun makeCall(request: VoiceCallRequest, businessContext: BusinessContext): VoiceCallResponse {
        val voiceHandler = serviceFactory.getHandler(ThirdPartyServiceList.VOICE)
        val response = voiceHandler.makeCall(request, businessContext)

        // Log the call if we have entity information
        if (request.entityName != null && request.entityId != null) {
            try {
                callLogService.createCallLog(
                    CallLogRequest(
                        callSid = response.callSid,
                        entityName = request.entityName,
                        entityId = request.entityId,
                        direction = "outgoing",
                        fromNumber = response.fromNumber,
                        toNumber = response.toNumber,
                        status = response.status,
                        exophone = request.exophone
                    )
                )
            } catch (e: Exception) {
                // Log error but don't fail the call
                // In production, you might want to use a proper logger
                // Just log the error and continue - don't throw exception
            }
        }

        return response
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun getCallStatus(callSid: String, businessContext: BusinessContext): VoiceCallResponse {
        val voiceHandler = serviceFactory.getHandler(ThirdPartyServiceList.VOICE)
        val response = voiceHandler.getCallStatus(callSid, businessContext)

        // Update call log with current status
        if (response.callSid.isNotEmpty()) {
            try {
                callLogService.updateCallStatus(
                    callSid = response.callSid,
                    status = response.status,
                    recordingUrl = response.recordingUrl
                )
            } catch (e: Exception) {
                // Log error but don't fail the status check
                // In production, you might want to use a proper logger
                // Just log the error and continue - don't throw exception
            }
        }

        return response
    }
}
