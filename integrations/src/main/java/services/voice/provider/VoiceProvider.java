package services.voice.provider;

import framework.ThirdPartyProvider;
import framework.config.BusinessContext;
import framework.core.data.ThirdPartyConfig;
import services.voice.dto.VoiceCallRequest;
import services.voice.dto.VoiceCallResponse;
import services.voice.provider.exotel.data.ExotelConfiguration;

public interface VoiceProvider extends ThirdPartyProvider<ExotelConfiguration> {
    VoiceCallResponse makeCall(
            VoiceCallRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );
    
    VoiceCallResponse getCallStatus(
            String callSid,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );
}

