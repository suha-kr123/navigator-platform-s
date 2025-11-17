package com.nivasafinance.services.whatsapp.provider;

import com.nivasafinance.integrations.framework.ThirdPartyProvider;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.wati.data.WatiConfiguration;

public interface WhatsAppProvider extends ThirdPartyProvider<WatiConfiguration> {
    
    WhatsAppTemplateResponse sendTemplate(
        WhatsAppTemplateRequest request,
        ThirdPartyConfig config,
        BusinessContext businessContext
    );
    
    WhatsAppTemplateResponse getTemplateStatus(
        String phoneNumber,
        ThirdPartyConfig config,
        BusinessContext businessContext
    );
}

