package framework.core.logger;

import framework.config.BusinessContext;
import framework.core.data.ApiContext;
import framework.core.entity.ThirdPartyResponseLog;
import framework.core.repository.ThirdPartyResponseLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ThirdPartyRequestResponseLoggerImpl implements ThirdPartyRequestResponseLogger {

    private final ThirdPartyResponseLogRepository thirdPartyResponseLogRepository;

    @Autowired
    public ThirdPartyRequestResponseLoggerImpl(ThirdPartyResponseLogRepository thirdPartyResponseLogRepository) {
        this.thirdPartyResponseLogRepository = thirdPartyResponseLogRepository;
    }

    @Override
    public UUID registerRequest(
            BusinessContext businessContext,
            ApiContext apiContext,
            HttpMethod requestMethod,
            String url,
            String requestBody) {
        ThirdPartyResponseLog logEntry = new ThirdPartyResponseLog();
        logEntry.setEntityType(1); // Default entity type
        logEntry.setEntityId(businessContext.getEntityId());
        logEntry.setRequestMethod(requestMethod.toString());
        logEntry.setUrl(url);
        logEntry.setRequest(requestBody);
        logEntry.setBusinessPurpose(businessContext.getBusinessPurpose());
        logEntry.setBusinessEntityName(businessContext.getEntityName());
        logEntry.setApiPurpose(apiContext.getApiPurpose());

        ThirdPartyResponseLog savedLog = thirdPartyResponseLogRepository.save(logEntry);
        return savedLog.getId() != null ? savedLog.getId() : UUID.randomUUID();
    }

    @Override
    public UUID registerThirdPartyRequest(
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
            UUID thirdPartyRequestId,
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

