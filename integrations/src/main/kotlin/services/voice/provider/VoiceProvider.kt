package services.voice.provider

import framework.ThirdPartyProvider
import framework.config.BusinessContext
import framework.core.data.ThirdPartyConfig
import services.voice.dto.VoiceCallRequest
import services.voice.dto.VoiceCallResponse
import services.voice.provider.exotel.data.ExotelConfiguration

interface VoiceProvider : ThirdPartyProvider<ExotelConfiguration> {
    fun makeCall(
        request: VoiceCallRequest,
        config: ThirdPartyConfig,
        businessContext: BusinessContext
    ): VoiceCallResponse
    fun getCallStatus(callSid: String, config: ThirdPartyConfig, businessContext: BusinessContext): VoiceCallResponse
}
