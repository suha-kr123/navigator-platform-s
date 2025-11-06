package services.voice.provider.exotel;

import org.springframework.web.client.RestTemplate;
import services.voice.dto.VoiceCallRequest;
import services.voice.dto.VoiceCallResponse;
import services.voice.provider.exotel.data.ExotelConfiguration;

public class ExotelApiClient {

    private final ExotelHttpClient httpClient;

    public ExotelApiClient() {
        this.httpClient = new ExotelHttpClient(new RestTemplate());
    }

    public ExotelApiClient(ExotelHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public VoiceCallResponse makeCall(ExotelConfiguration config, VoiceCallRequest request) {
        return httpClient.makeCall(config, request);
    }

    public VoiceCallResponse getCallStatus(ExotelConfiguration config, String callSid) {
        return httpClient.getCallStatus(config, callSid);
    }
}

