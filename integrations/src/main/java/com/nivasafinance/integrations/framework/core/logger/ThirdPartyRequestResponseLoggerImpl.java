package com.nivasafinance.integrations.framework.core.logger;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ApiContext;
import com.nivasafinance.integrations.framework.core.entity.ThirdPartyResponseLog;
import com.nivasafinance.integrations.framework.core.repository.ThirdPartyResponseLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class ThirdPartyRequestResponseLoggerImpl implements ThirdPartyRequestResponseLogger {

    private final ThirdPartyResponseLogRepository thirdPartyResponseLogRepository;

    @Autowired
    public ThirdPartyRequestResponseLoggerImpl(ThirdPartyResponseLogRepository thirdPartyResponseLogRepository) {
        this.thirdPartyResponseLogRepository = thirdPartyResponseLogRepository;
    }

    @Override
    public Long registerRequest(
            BusinessContext businessContext,
            ApiContext apiContext,
            HttpMethod requestMethod,
            String url,
            String requestBody) {
        ThirdPartyResponseLog logEntry = new ThirdPartyResponseLog();
        logEntry.setEntityType(businessContext.getEntityName()); // Default entity type
        logEntry.setEntityId(businessContext.getEntityId());
        logEntry.setRequestMethod(requestMethod.toString());
        logEntry.setUrl(url);
        logEntry.setRequest(requestBody);
        logEntry.setBusinessPurpose(businessContext.getBusinessPurpose());
        logEntry.setProviderName(apiContext.getProviderName());
        logEntry.setProviderRefId(apiContext.getProviderConfigId().toString());

        ThirdPartyResponseLog savedLog = thirdPartyResponseLogRepository.save(logEntry);
        return savedLog.getId();
    }

    @Override
    public Long registerThirdPartyRequest(
            BusinessContext businessContext,
            ApiContext apiContext,
            HttpMethod requestMethod,
            String url,
            String requestBody) {
        // For now, this is the same as registerRequest
        return registerRequest(businessContext, apiContext, requestMethod, url, requestBody);
    }

    @Override
    public void registerResponse(
            Long thirdPartyRequestId,
            String responseBody,
            long responseTimeInMs,
            int requestStatus) {
        var logEntry = thirdPartyResponseLogRepository.findById(thirdPartyRequestId).orElse(null);
        if (logEntry != null) {
            logEntry.setResponse(responseBody);
            logEntry.setResponseTimeInMs(responseTimeInMs);
            logEntry.setHttpStatusCode(requestStatus);
            thirdPartyResponseLogRepository.save(logEntry);
        }
    }
}

