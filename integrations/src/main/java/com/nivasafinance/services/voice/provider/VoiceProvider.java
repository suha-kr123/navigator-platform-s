package com.nivasafinance.services.voice.provider;

import com.nivasafinance.integrations.framework.ThirdPartyProvider;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.services.voice.dto.VoiceCallRequest;
import com.nivasafinance.services.voice.dto.VoiceCallResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelConfiguration;

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

