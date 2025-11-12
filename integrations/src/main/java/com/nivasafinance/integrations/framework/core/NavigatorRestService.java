package com.nivasafinance.integrations.framework.core;

import com.nivasafinance.integrations.framework.core.data.IntegrationResponse;
import com.nivasafinance.integrations.framework.core.data.IntegrationRestRequest;
import com.nivasafinance.integrations.framework.core.logger.ThirdPartyRequestResponseLogger;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.UUID;

@Service
public class NavigatorRestService {

    private final RestTemplate restTemplate;
    private final ThirdPartyRequestResponseLogger thirdPartyRequestResponseLogger;

    @Autowired
    public NavigatorRestService(
            RestTemplateBuilder restTemplateBuilder,
            ThirdPartyRequestResponseLogger thirdPartyRequestResponseLogger) {
        this.restTemplate = restTemplateBuilder.build();
        this.thirdPartyRequestResponseLogger = thirdPartyRequestResponseLogger;
    }

    public <T> IntegrationResponse doRestRequest(IntegrationRestRequest<T> request) {
        return doRestRequest(request, this.restTemplate);
    }

    public <T> IntegrationResponse doRestRequest(IntegrationRestRequest<T> request, RestTemplate restTemplate) {
        String requestBody = request.getRequestBody() != null ? request.getRequestBody().toString() : null;
        UUID requestId = thirdPartyRequestResponseLogger.registerRequest(
                request.getBusinessContext(),
                request.getApiContext(),
                request.getMethod(),
                request.getUrl(),
                requestBody
        );
        RegisterResponseBody registerResponseBody = new RegisterResponseBody(requestId, new StopWatch());
        StopWatch stopWatch = registerResponseBody.getStopWatch();
        try {
            stopWatch.start();
            return performRestExchange(request, registerResponseBody, restTemplate);
        } catch (HttpStatusCodeException e) {
            registerResponseBody.setHttpStatusCode(e.getStatusCode().value());
            registerResponseBody.setResponseString(e.getResponseBodyAsString());
            return handleHttpStatusCodeException(e);
        } catch (RestClientException e) {
            return IntegrationResponse.clientErrorResponse(e.getMessage());
        } finally {
            if (stopWatch.isStarted()) {
                stopWatch.stop();
            }
            long responseTimeInMs = stopWatch.getTime();
            if (request.isResponseLoggable()) {
                this.thirdPartyRequestResponseLogger.registerResponse(
                        registerResponseBody.getRequestLogId(),
                        registerResponseBody.getResponseString() != null ? registerResponseBody.getResponseString() : "",
                        responseTimeInMs,
                        registerResponseBody.getHttpStatusCode()
                );
            }
        }
    }

    private IntegrationResponse handleHttpStatusCodeException(HttpStatusCodeException e) {
        if (e.getStatusCode().is4xxClientError()) {
            return IntegrationResponse.clientErrorResponse(
                    HttpStatus.valueOf(e.getStatusCode().value()),
                    e.getResponseBodyAsString()
            );
        } else if (e.getStatusCode().is5xxServerError() || e.getStatusCode().is3xxRedirection()) {
            return IntegrationResponse.serverErrorResponse(
                    HttpStatus.valueOf(e.getStatusCode().value()),
                    e.getResponseBodyAsString()
            );
        } else {
            return IntegrationResponse.clientErrorResponse(
                    HttpStatus.valueOf(e.getStatusCode().value()),
                    e.getResponseBodyAsString()
            );
        }
    }

    private <T> IntegrationResponse performRestExchange(
            IntegrationRestRequest<T> request,
            RegisterResponseBody registerResponseBody,
            RestTemplate restTemplate) {
        IntegrationResponse integrationResponse;
        if (request.isConvertToBase64()) {
            var response = restTemplate.exchange(
                    request.getUri(),
                    request.getMethod(),
                    request.getHttpEntity(),
                    byte[].class
            );
            registerResponseBody.getStopWatch().stop();
            registerResponseBody.setResponseString(null);
            registerResponseBody.setHttpStatusCode(response.getStatusCode().value());
            integrationResponse = IntegrationResponse.successResponse(
                    HttpStatus.valueOf(response.getStatusCode().value()),
                    Base64.getEncoder().encodeToString(response.getBody())
            );
        } else {
            var response = restTemplate.exchange(
                    request.getUri(),
                    request.getMethod(),
                    request.getHttpEntity(),
                    String.class
            );
            registerResponseBody.getStopWatch().stop();
            registerResponseBody.setResponseString(response.getBody());
            registerResponseBody.setHttpStatusCode(response.getStatusCode().value());
            integrationResponse = IntegrationResponse.successResponse(
                    HttpStatus.valueOf(response.getStatusCode().value()),
                    response.getBody()
            );
        }
        return integrationResponse;
    }

    public static class RegisterResponseBody {
        private UUID requestLogId;
        private final StopWatch stopWatch;
        private String responseString;
        private int httpStatusCode = HttpStatus.OK.value();

        public RegisterResponseBody(UUID requestLogId, StopWatch stopWatch) {
            this.requestLogId = requestLogId;
            this.stopWatch = stopWatch;
        }

        public UUID getRequestLogId() {
            return requestLogId;
        }

        public void setRequestLogId(UUID requestLogId) {
            this.requestLogId = requestLogId;
        }

        public StopWatch getStopWatch() {
            return stopWatch;
        }

        public String getResponseString() {
            return responseString;
        }

        public void setResponseString(String responseString) {
            this.responseString = responseString;
        }

        public int getHttpStatusCode() {
            return httpStatusCode;
        }

        public void setHttpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
        }
    }
}

