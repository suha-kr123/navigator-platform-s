package com.nivasafinance.services.whatsapp.provider;

import com.nivasafinance.integrations.framework.ThirdPartyProvider;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;

public interface WhatsAppProvider<T> extends ThirdPartyProvider<T> {
    
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

