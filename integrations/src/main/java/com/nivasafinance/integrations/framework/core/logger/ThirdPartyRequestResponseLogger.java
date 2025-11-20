package com.nivasafinance.integrations.framework.core.logger;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ApiContext;
import org.springframework.http.HttpMethod;


public interface ThirdPartyRequestResponseLogger {

    Long registerRequest(
            BusinessContext businessContext,
            ApiContext apiContext,
            HttpMethod requestMethod,
            String url,
            String requestBody
    );

    Long registerThirdPartyRequest(
            BusinessContext businessContext,
            ApiContext apiContext,
            HttpMethod requestMethod,
            String url,
            String requestBody
    );

    void registerResponse(
            Long thirdPartyRequestId,
            String responseBody,
            long responseTimeInMs,
            int requestStatus
    );
}

