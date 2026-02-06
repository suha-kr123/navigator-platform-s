package com.nivasafinance.services.creditbureau.provider.crifhighmark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.core.NavigatorRestService;
import com.nivasafinance.integrations.framework.core.data.ApiContext;
import com.nivasafinance.integrations.framework.core.data.IntegrationResponse;
import com.nivasafinance.integrations.framework.core.data.IntegrationRestRequest;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationServerException;
import com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryRequest;
import com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus;
import com.nivasafinance.services.creditbureau.dto.CreditBureauProviderResponse;
import com.nivasafinance.services.creditbureau.dto.PullEnquiryRequest;
import com.nivasafinance.services.creditbureau.provider.CreditBureauProvider;
import com.nivasafinance.services.creditbureau.provider.crifhighmark.data.CrifConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrifHighmarkCreditBureauProvider implements CreditBureauProvider {

    private final NavigatorRestService navigatorRestService;
    private final CrifRequestBuilder crifRequestBuilder;
    private final CrifResponseHandler crifResponseHandler;
    private final CrifConfiguration crifConfiguration;
    private final SecretManagerService secretManagerService;
    private final ObjectMapper objectMapper;

    @Override
    public ThirdPartyProviderList getKey() {
        return ThirdPartyProviderList.CRIF_HIGHMARK;
    }

    @Override
    public CrifConfiguration setupConfiguration(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return fromApplicationProperties();
        }
        if (map.containsKey("secret_key")) {
            Map<String, Object> secretMap = secretManagerService.getSecret(map.get("secret_key"));
            return new CrifConfiguration(
                    secretMap.get("appId").toString(),
                    secretMap.get("merchantId").toString(),
                    secretMap.get("userId").toString(),
                    secretMap.get("customerId").toString(),
                    secretMap.get("productCode").toString(),
                    secretMap.get("password").toString(),
                    secretMap.get("baseUrl").toString()
            );
        }
        return new CrifConfiguration(
                map.get("appId"),
                map.get("merchantId"),
                map.get("userId"),
                map.get("customerId"),
                map.get("productCode"),
                map.get("password"),
                map.get("baseUrl")
        );
    }

    private CrifConfiguration fromApplicationProperties() {
        return new CrifConfiguration(
                crifConfiguration.getAppId(),
                crifConfiguration.getMerchantId(),
                crifConfiguration.getUserId(),
                crifConfiguration.getCustomerId(),
                crifConfiguration.getProductCode(),
                crifConfiguration.getPassword(),
                crifConfiguration.getBaseUrl()
        );
    }

    @Override
    public CreditBureauProviderResponse initiateEnquiry(
            CreditBureauEnquiryRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        throw new UnsupportedOperationException("CRIF Highmark enquiry initiation not yet implemented");
    }

    @Override
    public CreditBureauProviderResponse getEnquiryStatus(
            String enquiryId,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        throw new UnsupportedOperationException("CRIF Highmark enquiry status check not yet implemented");
    }

    @Override
    public CreditBureauProviderResponse pullEnquiry(
            PullEnquiryRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        log.info("Starting CRIF pull enquiry for enquiry ID: {}", request.getEnquiryId());

        if (request.getPersonData() == null) {
            throw new NavigatorIntegrationServerException("Person data is required in PullEnquiryRequest");
        }

        CrifConfiguration crifConfig = setupConfiguration(config.getConfigurations());

        String orderId = request.getEnquiryIdentifier();
        String accessCode = crifRequestBuilder.generateAccessCode(
                crifConfig.getUserId(),
                crifConfig.getCustomerId(),
                crifConfig.getProductCode(),
                crifConfig.getPassword()
        );

        List<AddressData> addresses = request.getPersonData().getAddresses();
        List<IdentifierData> identifiers = request.getPersonData().getIdentifiers();

        CreditBureauProviderResponse.CreditBureauProviderResponseBuilder responseBuilder = CreditBureauProviderResponse.builder();
        String stage1Payload = null;

        try {
            // Stage-I: Initiate
            stage1Payload = crifRequestBuilder.buildStage1Payload(
                    request.getPersonData(),
                    crifConfig.getCustomerId(),
                    crifConfig.getProductCode(),
                    addresses,
                    identifiers
            );
            responseBuilder.stage1Request(stage1Payload);
            log.debug("Stage-I payload built, length: {}", stage1Payload != null ? stage1Payload.length() : 0);

            IntegrationResponse stage1Response = executeStage1(stage1Payload, orderId, accessCode, crifConfig, config, businessContext);
            String reportId = crifResponseHandler.parseStage1Response(stage1Response.getResponseBody());
            responseBuilder.reportId(reportId);

            log.debug("Stage-I successful: reportId={}", reportId);

            // Stage-II: Authorization
            String stage2Payload = crifRequestBuilder.buildStage2Payload(orderId, reportId, accessCode, "Y");
            IntegrationResponse stage2Response = executeStage2(stage2Payload, orderId, reportId, accessCode, crifConfig, config, businessContext);
            crifResponseHandler.parseStage2Response(stage2Response.getResponseBody());

            log.debug("Stage-II successful");

            // Stage-III: Fetch Report
            String stage3Payload = crifRequestBuilder.buildStage3Payload(orderId, reportId, accessCode);
            IntegrationResponse stage3Response = executeStage3(stage3Payload, orderId, reportId, accessCode, crifConfig, config, businessContext);
            responseBuilder.creditBureauReportJson(stage3Response.getResponseBody());

            Map<String, Object> reportDetails = parseReportDetails(stage3Response.getResponseBody());
            responseBuilder.reportDetails(reportDetails);
            // Status will be determined by credit bureau module after parsing
            // Return SUCCESS if API call succeeded (non-empty response)
            responseBuilder.status(CreditBureauEnquiryStatus.SUCCESS);

            log.info("CRIF pull enquiry completed: enquiry ID={}, status will be determined after parsing", request.getEnquiryId());
            return responseBuilder.enquiryId(String.valueOf(request.getEnquiryId())).build();

        } catch (CrifNoHitException e) {
            log.warn("CRIF Stage-II returned S09 (no hit) for enquiry ID: {}", request.getEnquiryId());
            if (stage1Payload != null && !stage1Payload.trim().isEmpty()) {
                responseBuilder.stage1Request(stage1Payload);
            }
            responseBuilder.status(CreditBureauEnquiryStatus.NO_HIT);
            responseBuilder.error(e.getMessage());
            return responseBuilder.enquiryId(String.valueOf(request.getEnquiryId())).build();
        } catch (CrifDataMismatchException e) {
            log.warn("CRIF Stage-II returned S11 (data mismatch) for enquiry ID: {}", request.getEnquiryId());
            if (stage1Payload != null && !stage1Payload.trim().isEmpty()) {
                responseBuilder.stage1Request(stage1Payload);
            }
            responseBuilder.status(CreditBureauEnquiryStatus.DATA_MISMATCH);
            responseBuilder.error(e.getMessage());
            return responseBuilder.enquiryId(String.valueOf(request.getEnquiryId())).build();
        } catch (Exception e) {
            log.error("CRIF pull enquiry failed for enquiry ID: {}", request.getEnquiryId(), e);
            // Always preserve stage1Request if it was built, even on failure
            if (stage1Payload != null && !stage1Payload.trim().isEmpty()) {
                responseBuilder.stage1Request(stage1Payload);
                log.debug("Preserved stage1Request in error handler: length={}", stage1Payload.length());
            } else {
                log.warn("stage1Payload is null or empty, cannot preserve request. stage1Payload is null: {}", stage1Payload == null);
            }
            responseBuilder.status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.FAILED);
            responseBuilder.error(e.getMessage());
            return responseBuilder.enquiryId(String.valueOf(request.getEnquiryId())).build();
        }
    }

    private IntegrationResponse executeStage1(
            String payload,
            String orderId,
            String accessCode,
            CrifConfiguration crifConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        HttpHeaders headers = buildStage1Headers(orderId, accessCode, crifConfig);
        ApiContext apiContext = new ApiContext(getKey().getProvideName(), config.getId());

        IntegrationRestRequest<String> request = new IntegrationRestRequest<>();
        request.setUrl(crifConfig.getBaseUrl() + "/initiate");
        request.setMethod(HttpMethod.POST);
        request.setBusinessContext(businessContext);
        request.setApiContext(apiContext);
        request.setQueryParams(new LinkedMultiValueMap<>());
        request.setRequestBody(payload);
        request.setHeaders(headers);

        IntegrationResponse response = navigatorRestService.doRestRequest(request);
        if (!response.isSuccess()) {
            throw new NavigatorIntegrationServerException("Stage-I failed: " + response.getErrorMessage());
        }
        return response;
    }

    private IntegrationResponse executeStage2(
            String payload,
            String orderId,
            String reportId,
            String accessCode,
            CrifConfiguration crifConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        HttpHeaders headers = buildStage2Headers(orderId, reportId, accessCode, crifConfig);
        ApiContext apiContext = new ApiContext(getKey().getProvideName(), config.getId());

        IntegrationRestRequest<String> request = new IntegrationRestRequest<>();
        request.setUrl(crifConfig.getBaseUrl() + "/response");
        request.setMethod(HttpMethod.POST);
        request.setBusinessContext(businessContext);
        request.setApiContext(apiContext);
        request.setQueryParams(new LinkedMultiValueMap<>());
        request.setRequestBody(payload);
        request.setHeaders(headers);

        IntegrationResponse response = navigatorRestService.doRestRequest(request);
        if (!response.isSuccess()) {
            throw new NavigatorIntegrationServerException("Stage-II failed: " + response.getErrorMessage());
        }
        return response;
    }

    private IntegrationResponse executeStage3(
            String payload,
            String orderId,
            String reportId,
            String accessCode,
            CrifConfiguration crifConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        HttpHeaders headers = buildStage3Headers(orderId, reportId, accessCode, crifConfig);
        ApiContext apiContext = new ApiContext(getKey().getProvideName(), config.getId());

        IntegrationRestRequest<String> request = new IntegrationRestRequest<>();
        request.setUrl(crifConfig.getBaseUrl() + "/response");
        request.setMethod(HttpMethod.POST);
        request.setBusinessContext(businessContext);
        request.setApiContext(apiContext);
        request.setQueryParams(new LinkedMultiValueMap<>());
        request.setRequestBody(payload);
        request.setHeaders(headers);

        IntegrationResponse response = navigatorRestService.doRestRequest(request);
        if (!response.isSuccess()) {
            throw new NavigatorIntegrationServerException("Stage-III failed: " + response.getErrorMessage());
        }
        return response;
    }

    private HttpHeaders buildStage1Headers(String orderId, String accessCode, CrifConfiguration crifConfig) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.ACCEPT, "*/*");
        headers.set("appID", crifConfig.getAppId());
        headers.set("merchantID", crifConfig.getMerchantId());
        headers.set("orderId", orderId);
        headers.set("accessCode", accessCode);
        return headers;
    }

    private HttpHeaders buildStage2Headers(String orderId, String reportId, String accessCode, CrifConfiguration crifConfig) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.ACCEPT, "*/*");
        headers.set("appID", crifConfig.getAppId());
        headers.set("merchantID", crifConfig.getMerchantId());
        headers.set("orderId", orderId);
        headers.set("reportID", reportId); // Note: CRIF uses capital "ID" in header name
        headers.set("requestType", "Authorization");
        headers.set("accessCode", accessCode);
        return headers;
    }

    private HttpHeaders buildStage3Headers(String orderId, String reportId, String accessCode, CrifConfiguration crifConfig) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.ACCEPT, "*/*");
        headers.set("appID", crifConfig.getAppId());
        headers.set("merchantID", crifConfig.getMerchantId());
        headers.set("orderId", orderId);
        headers.set("reportID", reportId);
        headers.set("accessCode", accessCode);
        return headers;
    }

    private Map<String, Object> parseReportDetails(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            Map<String, Object> reportDetails = new HashMap<>();
            jsonNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode fieldValue = jsonNode.get(fieldName);
                if (fieldValue != null) {
                    reportDetails.put(fieldName, objectMapper.convertValue(fieldValue, Object.class));
                }
            });
            return reportDetails;
        } catch (Exception e) {
            log.warn("Failed to parse report details from response", e);
            return new HashMap<>();
        }
    }
}

