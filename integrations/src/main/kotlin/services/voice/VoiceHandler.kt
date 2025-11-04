package services.voice

import framework.ThirdPartyHandler
import framework.config.BusinessContext
import framework.config.ThirdPartyServiceList
import framework.core.exception.VoiceHandlerException
import framework.runner.ServiceRunner
import org.springframework.stereotype.Service
import services.voice.dto.VoiceCallRequest
import services.voice.dto.VoiceCallResponse
import services.voice.provider.VoiceProvider

@Service
class VoiceHandler(
    services: Set<VoiceProvider>
) : ThirdPartyHandler() {

    private val servicesMap: MutableMap<String, VoiceProvider> = HashMap()

    init {
        services.associateByTo(servicesMap) { it.getKey().provideName }
    }

    fun makeCall(voiceCallRequest: VoiceCallRequest, businessContext: BusinessContext): VoiceCallResponse {
        val primaryProvider = servicesMap[runConfig.primaryConfig.provider]
            ?: throw VoiceHandlerException("Error fetching ${getKey().serviceName}")
        val fallbackProvider = runConfig.fallbackConfig?.let { servicesMap[it.provider] }
        return ServiceRunner<VoiceProvider?, VoiceCallRequest>(primaryProvider, fallbackProvider, runConfig.retries)
            .invokeService("makeCall", voiceCallRequest, runConfig, businessContext) as VoiceCallResponse
    }

    fun getCallStatus(callSid: String, businessContext: BusinessContext): VoiceCallResponse {
        val primaryProvider = servicesMap[runConfig.primaryConfig.provider]
            ?: throw VoiceHandlerException("Error fetching ${getKey().serviceName}")
        val fallbackProvider = runConfig.fallbackConfig?.let { servicesMap[it.provider] }
        return ServiceRunner<VoiceProvider?, String>(primaryProvider, fallbackProvider, runConfig.retries)
            .invokeService("getCallStatus", callSid, runConfig, businessContext) as VoiceCallResponse
    }

    override fun getKey(): ThirdPartyServiceList = ThirdPartyServiceList.VOICE
}
