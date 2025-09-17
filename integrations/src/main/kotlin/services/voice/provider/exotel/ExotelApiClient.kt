package services.voice.provider.exotel

import org.springframework.web.client.RestTemplate
import services.voice.dto.VoiceCallRequest
import services.voice.dto.VoiceCallResponse
import services.voice.provider.exotel.data.ExotelConfiguration

class ExotelApiClient(private val httpClient: ExotelHttpClient = ExotelHttpClient(RestTemplate())) {

    fun makeCall(config: ExotelConfiguration, request: VoiceCallRequest): VoiceCallResponse {
        return httpClient.makeCall(config, request)
    }

    fun getCallStatus(config: ExotelConfiguration, callSid: String): VoiceCallResponse {
        return httpClient.getCallStatus(config, callSid)
    }
}
