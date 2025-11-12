package com.nivasafinance.integrations.framework.core.logger;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ApiContext;
import org.springframework.http.HttpMethod;

import java.util.UUID;

public interface ThirdPartyRequestResponseLogger {

    UUID registerRequest(
            BusinessContext businessContext,
            ApiContext apiContext,
            HttpMethod requestMethod,
            String url,
            String requestBody
    );

    UUID registerThirdPartyRequest(
            BusinessContext businessContext,
            ApiContext apiContext,
            HttpMethod requestMethod,
            String url,
            String requestBody
    );

    void registerResponse(
            UUID thirdPartyRequestId,
            String responseBody,
            long responseTimeInMs,
            int requestStatus
    );
}

