package com.nivasafinance.services.voice.provider;

import com.nivasafinance.integrations.framework.ThirdPartyProvider;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.services.voice.dto.*;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelConfiguration;

public interface VoiceProvider extends ThirdPartyProvider<ExotelConfiguration> {
    VoiceCallResponse makeCall(
            VoiceCallRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );
    
    VoiceGetCallStatusResponse getCallStatus(
            String callSid,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );

    VoiceCreateListResponse uploadCSVList(
            VoiceCreateListRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );

    VoiceCSVUploadStatusResponse getCSVUploadStatus(
            VoiceCSVUploadStatusRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );

    VoiceCampaignResponse createCampaign(
            VoiceCampaignRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );

    VoiceCampaignResponse getCampaignDetails(
            VoiceGetCampaignDetailsRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );
}

