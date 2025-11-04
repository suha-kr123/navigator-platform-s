package services.voice.provider.exotel

import framework.config.BusinessContext
import framework.config.ThirdPartyProviderList
import framework.core.data.ThirdPartyConfig
import org.springframework.stereotype.Component
import services.voice.dto.VoiceCallRequest
import services.voice.dto.VoiceCallResponse
import services.voice.provider.VoiceProvider
import services.voice.provider.exotel.data.ExotelConfiguration

@Component
class ExotelVoiceProvider : VoiceProvider {

    private val apiClient = ExotelApiClient()

    override fun getKey(): ThirdPartyProviderList = ThirdPartyProviderList.EXOTEL

    override fun setupConfiguration(map: Map<String, String>): ExotelConfiguration {
        return ExotelConfiguration(
            accountSid = map["accountSid"].orEmpty(),
            authToken = map["authToken"].orEmpty(),
            subdomain = map["subdomain"]?.takeIf { it.isNotEmpty() } ?: "api.exotel.com",
            callerId = map["callerId"].orEmpty(),
            webhookUrl = map["webhookUrl"].orEmpty(),
            apiKey = map["apiKey"].orEmpty(),
            apiToken = map["apiToken"].orEmpty(),
            recordingEnabled = (map["recordingEnabled"] ?: "true").toBooleanStrictOrNull() ?: true,
            maxCallDuration = (map["maxCallDuration"] ?: "3600").toIntOrNull() ?: 3600,
            retryAttempts = (map["retryAttempts"] ?: "3").toIntOrNull() ?: 3,
            timeout = (map["timeout"] ?: "30").toIntOrNull() ?: 30
        )
    }

    override fun makeCall(
        request: VoiceCallRequest,
        config: ThirdPartyConfig,
        businessContext: BusinessContext
    ): VoiceCallResponse {
        val exotelConfig = setupConfiguration(config.configurations)

        // Validate phone numbers
        require(request.fromNumber.isNotBlank()) { "From number cannot be empty" }
        require(request.toNumber.isNotBlank()) { "To number cannot be empty" }

        val exotelResponse = apiClient.makeCall(exotelConfig, request)

        return exotelResponse
    }

    override fun getCallStatus(
        callSid: String,
        config: ThirdPartyConfig,
        businessContext: BusinessContext
    ): VoiceCallResponse {
        val exotelConfig = setupConfiguration(config.configurations)
        val exotelResponse = apiClient.getCallStatus(exotelConfig, callSid)

        return exotelResponse
    }
}
