package com.nivasafinance.services.creditbureau.provider;

import com.nivasafinance.integrations.framework.ThirdPartyProvider;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryRequest;
import com.nivasafinance.services.creditbureau.dto.CreditBureauProviderResponse;
import com.nivasafinance.services.creditbureau.dto.PullEnquiryRequest;
import com.nivasafinance.services.creditbureau.provider.crifhighmark.data.CrifConfiguration;

public interface CreditBureauProvider extends ThirdPartyProvider<CrifConfiguration> {
    CreditBureauProviderResponse initiateEnquiry(
            CreditBureauEnquiryRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );
    
    CreditBureauProviderResponse getEnquiryStatus(
            String enquiryId,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );
    
    CreditBureauProviderResponse pullEnquiry(
            PullEnquiryRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext
    );
}

