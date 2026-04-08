package com.nivasafinance.services.creditbureau;

import com.nivasafinance.integrations.framework.ThirdPartyHandler;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.data.RunConfig;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.exception.ServiceInvocationException;
import com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryRequest;
import com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus;
import com.nivasafinance.services.creditbureau.dto.CreditBureauProviderResponse;
import com.nivasafinance.services.creditbureau.dto.PullEnquiryRequest;
import com.nivasafinance.services.creditbureau.provider.CreditBureauProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditBureauHandlerTest {

    private static final String PROVIDER_NAME = "crif_highmark";

    private CreditBureauProvider primaryProvider;
    private CreditBureauHandler handler;
    private RunConfig runConfig;
    private ThirdPartyConfig primaryConfig;
    private BusinessContext businessContext;

    @BeforeEach
    void setUp() {
        primaryProvider = mock(CreditBureauProvider.class);
        when(primaryProvider.getKey()).thenReturn(ThirdPartyProviderList.CRIF_HIGHMARK);

        handler = new CreditBureauHandler(Set.of(primaryProvider));

        primaryConfig = new ThirdPartyConfig(1L, "primary", PROVIDER_NAME, Map.of());
        runConfig = new RunConfig(primaryConfig, null, 1);
        ReflectionTestUtils.setField(handler, "runConfig", runConfig);

        businessContext = new BusinessContext("LOAN", 1L, "CREDIT_CHECK");
    }

    // ── getKey ──────────────────────────────────────────────────────

    @Test
    void getKey_always_returnsCreditBureau() {
        assertEquals(ThirdPartyServiceList.CREDIT_BUREAU, handler.getKey(),
                "Handler key should be CREDIT_BUREAU");
    }

    // ── constructor ─────────────────────────────────────────────────

    @Test
    void constructor_withProviders_resolvesProviderByName() {
        CreditBureauProviderResponse expectedResponse = buildResponse("ENQ-001", CreditBureauEnquiryStatus.INITIATED);
        when(primaryProvider.initiateEnquiry(
                any(CreditBureauEnquiryRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        CreditBureauProviderResponse result = handler.initiateEnquiry(
                CreditBureauEnquiryRequest.builder().personId(1L).build(), businessContext);

        assertEquals(expectedResponse, result,
                "Provider registered during construction should be resolved by name from config");
    }

    @Test
    void constructor_withEmptyProviderSet_failsOnProviderLookup() {
        CreditBureauHandler emptyHandler = new CreditBureauHandler(Collections.emptySet());
        ReflectionTestUtils.setField(emptyHandler, "runConfig", runConfig);

        assertThrows(ServiceInvocationException.class,
                () -> emptyHandler.initiateEnquiry(
                        CreditBureauEnquiryRequest.builder().build(), businessContext),
                "Empty provider set should cause ServiceInvocationException on provider lookup");
    }

    // ── initiateEnquiry ─────────────────────────────────────────────

    @Test
    void initiateEnquiry_withValidProvider_returnsProviderResponse() {
        CreditBureauProviderResponse expectedResponse = buildResponse("ENQ-001", CreditBureauEnquiryStatus.INITIATED);
        when(primaryProvider.initiateEnquiry(
                any(CreditBureauEnquiryRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        CreditBureauEnquiryRequest request = CreditBureauEnquiryRequest.builder()
                .personId(100L).phoneNumber("9876543210").build();

        CreditBureauProviderResponse result = handler.initiateEnquiry(request, businessContext);

        assertEquals(expectedResponse, result,
                "Should return the response from the primary provider");
    }

    @Test
    void initiateEnquiry_whenPrimaryProviderNotFound_throwsServiceInvocationException() {
        primaryConfig.setProvider("unknown_provider");

        ServiceInvocationException ex = assertThrows(ServiceInvocationException.class,
                () -> handler.initiateEnquiry(
                        CreditBureauEnquiryRequest.builder().build(), businessContext),
                "Should throw ServiceInvocationException when primary provider is not in the map");
        assertTrue(ex.getMessage().contains("credit_bureau"),
                "Exception message should contain the service name");
    }

    @Test
    void initiateEnquiry_withFallbackConfigPresent_returnsResponse() {
        ThirdPartyConfig fallbackConfig = new ThirdPartyConfig(2L, "fallback", PROVIDER_NAME, Map.of());
        runConfig.setFallbackConfig(fallbackConfig);

        CreditBureauProviderResponse expectedResponse = buildResponse("ENQ-002", CreditBureauEnquiryStatus.INITIATED);
        when(primaryProvider.initiateEnquiry(
                any(CreditBureauEnquiryRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        CreditBureauProviderResponse result = handler.initiateEnquiry(
                CreditBureauEnquiryRequest.builder().personId(1L).build(), businessContext);

        assertEquals(expectedResponse, result,
                "Should return response when fallback config is also present");
    }

    @Test
    void initiateEnquiry_withNullFallbackConfig_returnsResponse() {
        runConfig.setFallbackConfig(null);

        CreditBureauProviderResponse expectedResponse = buildResponse("ENQ-003", CreditBureauEnquiryStatus.SUCCESS);
        when(primaryProvider.initiateEnquiry(
                any(CreditBureauEnquiryRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        CreditBureauProviderResponse result = handler.initiateEnquiry(
                CreditBureauEnquiryRequest.builder().personId(1L).build(), businessContext);

        assertEquals(expectedResponse, result,
                "Should succeed without fallback config when primary succeeds");
    }

    @Test
    void initiateEnquiry_whenProviderThrowsException_throwsCreditBureauHandlerException() {
        when(primaryProvider.initiateEnquiry(
                any(CreditBureauEnquiryRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenThrow(new RuntimeException("Provider error"));

        CreditBureauHandlerException ex = assertThrows(CreditBureauHandlerException.class,
                () -> handler.initiateEnquiry(
                        CreditBureauEnquiryRequest.builder().build(), businessContext),
                "Should wrap provider exception in CreditBureauHandlerException");
        assertTrue(ex.getMessage().contains("Error initiating credit bureau enquiry"),
                "Exception message should describe the initiateEnquiry operation");
    }

    // ── getEnquiryStatus ────────────────────────────────────────────

    @Test
    void getEnquiryStatus_withValidProvider_returnsProviderResponse() {
        CreditBureauProviderResponse expectedResponse = buildResponse("ENQ-001", CreditBureauEnquiryStatus.SUCCESS);
        when(primaryProvider.getEnquiryStatus(
                any(String.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        CreditBureauProviderResponse result = handler.getEnquiryStatus("ENQ-001", businessContext);

        assertEquals(expectedResponse, result,
                "Should return the status response from the primary provider");
    }

    @Test
    void getEnquiryStatus_whenPrimaryProviderNotFound_throwsServiceInvocationException() {
        primaryConfig.setProvider("unknown_provider");

        ServiceInvocationException ex = assertThrows(ServiceInvocationException.class,
                () -> handler.getEnquiryStatus("ENQ-001", businessContext),
                "Should throw ServiceInvocationException when primary provider is not in the map");
        assertTrue(ex.getMessage().contains("credit_bureau"),
                "Exception message should contain the service name");
    }

    @Test
    void getEnquiryStatus_withFallbackConfigPresent_returnsResponse() {
        ThirdPartyConfig fallbackConfig = new ThirdPartyConfig(2L, "fallback", PROVIDER_NAME, Map.of());
        runConfig.setFallbackConfig(fallbackConfig);

        CreditBureauProviderResponse expectedResponse = buildResponse("ENQ-001", CreditBureauEnquiryStatus.PROCESSING);
        when(primaryProvider.getEnquiryStatus(
                any(String.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        CreditBureauProviderResponse result = handler.getEnquiryStatus("ENQ-001", businessContext);

        assertEquals(expectedResponse, result,
                "Should return response when fallback config is present");
    }

    @Test
    void getEnquiryStatus_whenProviderThrowsException_throwsCreditBureauHandlerException() {
        when(primaryProvider.getEnquiryStatus(
                any(String.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenThrow(new RuntimeException("Status fetch error"));

        CreditBureauHandlerException ex = assertThrows(CreditBureauHandlerException.class,
                () -> handler.getEnquiryStatus("ENQ-001", businessContext),
                "Should wrap provider exception in CreditBureauHandlerException");
        assertTrue(ex.getMessage().contains("Error getting credit bureau enquiry status"),
                "Exception message should describe the getEnquiryStatus operation");
    }

    // ── pullEnquiry (2-arg) ─────────────────────────────────────────

    @Test
    void pullEnquiry_twoArgs_returnsResponseUsingPrimaryFromConfig() {
        CreditBureauProviderResponse expectedResponse = buildResponse("ENQ-001", CreditBureauEnquiryStatus.SUCCESS);
        when(primaryProvider.pullEnquiry(
                any(PullEnquiryRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        PullEnquiryRequest request = PullEnquiryRequest.builder().personId(1L).enquiryId(100L).build();

        CreditBureauProviderResponse result = handler.pullEnquiry(request, businessContext);

        assertEquals(expectedResponse, result,
                "Two-arg pullEnquiry should delegate and return response using primary provider from config");
    }

    // ── pullEnquiry (3-arg) with explicit provider name ─────────────

    @Test
    void pullEnquiry_withExplicitProviderName_usesSpecifiedProvider() {
        CreditBureauProviderResponse expectedResponse = buildResponse("ENQ-001", CreditBureauEnquiryStatus.SUCCESS);
        when(primaryProvider.pullEnquiry(
                any(PullEnquiryRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        PullEnquiryRequest request = PullEnquiryRequest.builder().personId(1L).enquiryId(100L).build();

        CreditBureauProviderResponse result = handler.pullEnquiry(request, businessContext, PROVIDER_NAME);

        assertEquals(expectedResponse, result,
                "Should resolve provider by explicit name and return its response");
    }

    @Test
    void pullEnquiry_withExplicitProviderNotFound_throwsServiceInvocationException() {
        PullEnquiryRequest request = PullEnquiryRequest.builder().build();

        ServiceInvocationException ex = assertThrows(ServiceInvocationException.class,
                () -> handler.pullEnquiry(request, businessContext, "nonexistent_provider"),
                "Should throw ServiceInvocationException when explicit provider is not found");
        assertTrue(ex.getMessage().contains("Provider not found"),
                "Exception message should indicate provider was not found");
    }

    // ── pullEnquiry (3-arg) with null/empty/blank provider name ─────

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void pullEnquiry_withNullEmptyOrBlankProviderName_usesPrimaryFromConfig(String providerName) {
        CreditBureauProviderResponse expectedResponse = buildResponse("ENQ-001", CreditBureauEnquiryStatus.SUCCESS);
        when(primaryProvider.pullEnquiry(
                any(PullEnquiryRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        PullEnquiryRequest request = PullEnquiryRequest.builder().personId(1L).build();

        CreditBureauProviderResponse result = handler.pullEnquiry(request, businessContext, providerName);

        assertEquals(expectedResponse, result,
                "Null, empty, or blank provider name should fall back to primary from config");
    }

    @Test
    void pullEnquiry_whenPrimaryFromConfigNotFound_throwsServiceInvocationException() {
        primaryConfig.setProvider("missing_provider");

        PullEnquiryRequest request = PullEnquiryRequest.builder().build();

        ServiceInvocationException ex = assertThrows(ServiceInvocationException.class,
                () -> handler.pullEnquiry(request, businessContext, null),
                "Should throw ServiceInvocationException when primary provider from config is not in the map");
        assertTrue(ex.getMessage().contains("credit_bureau"),
                "Exception message should contain the service name");
    }

    @Test
    void pullEnquiry_withFallbackConfigPresent_returnsResponse() {
        ThirdPartyConfig fallbackConfig = new ThirdPartyConfig(2L, "fallback", PROVIDER_NAME, Map.of());
        runConfig.setFallbackConfig(fallbackConfig);

        CreditBureauProviderResponse expectedResponse = buildResponse("ENQ-001", CreditBureauEnquiryStatus.SUCCESS);
        when(primaryProvider.pullEnquiry(
                any(PullEnquiryRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        PullEnquiryRequest request = PullEnquiryRequest.builder().personId(1L).build();

        CreditBureauProviderResponse result = handler.pullEnquiry(request, businessContext, null);

        assertEquals(expectedResponse, result,
                "Should return response when fallback config is present");
    }

    @Test
    void pullEnquiry_whenProviderThrowsException_throwsCreditBureauHandlerException() {
        when(primaryProvider.pullEnquiry(
                any(PullEnquiryRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenThrow(new RuntimeException("Pull error"));

        PullEnquiryRequest request = PullEnquiryRequest.builder().build();

        CreditBureauHandlerException ex = assertThrows(CreditBureauHandlerException.class,
                () -> handler.pullEnquiry(request, businessContext),
                "Should wrap provider exception in CreditBureauHandlerException");
        assertTrue(ex.getMessage().contains("Error pulling credit bureau enquiry"),
                "Exception message should describe the pullEnquiry operation");
    }

    // ── Helper ──────────────────────────────────────────────────────

    private CreditBureauProviderResponse buildResponse(String enquiryId, CreditBureauEnquiryStatus status) {
        return CreditBureauProviderResponse.builder()
                .enquiryId(enquiryId)
                .status(status)
                .build();
    }
}
