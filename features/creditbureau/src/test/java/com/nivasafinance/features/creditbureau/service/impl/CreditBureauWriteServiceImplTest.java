package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import com.nivasafinance.features.creditbureau.repository.CreditBureauRepositoryWrapper;
import com.nivasafinance.features.creditbureau.service.CreditBureauReportParser;
import com.nivasafinance.features.creditbureau.service.CreditBureauReportParserFactory;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.data.RunConfig;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.service.ThirdPartyServiceConfigReadService;
import com.nivasafinance.services.creditbureau.CreditBureauHandler;
import com.nivasafinance.services.creditbureau.dto.CreditBureauPersonData;
import com.nivasafinance.services.creditbureau.dto.CreditBureauProviderResponse;
import com.nivasafinance.services.creditbureau.dto.PullEnquiryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditBureauWriteServiceImplTest {

    @Mock
    private CreditBureauRepositoryWrapper creditBureauRepositoryWrapper;

    @Mock
    private ServiceFactory<CreditBureauHandler> serviceFactory;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ThirdPartyServiceConfigReadService thirdPartyServiceConfigReadService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private CreditBureauReportParserFactory creditBureauReportParserFactory;

    @InjectMocks
    private CreditBureauWriteServiceImpl creditBureauWriteService;

    private static final Long ENQUIRY_ID = 1L;
    private static final Long PERSON_ID = 100L;
    private static final String PROVIDER_NAME = "CRIF_HIGHMARK";

    private CreditBureauEnquiry enquiry;
    private UUID enquiryIdentifier;

    @BeforeEach
    void setUp() {
        enquiryIdentifier = UUID.randomUUID();

        enquiry = new CreditBureauEnquiry();
        enquiry.setId(ENQUIRY_ID);
        enquiry.setIdentifier(enquiryIdentifier);
        enquiry.setStatus(CreditBureauEnquiryStatus.INITIATED);
    }

    // ==================== initiateEnquiry() Tests ====================

    @Test
    void initiateEnquiry_savesEnquiryWithInitiatedStatus() {
        // Arrange
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(enquiry);

        // Act
        CreditBureauEnquiryResponse result = creditBureauWriteService.initiateEnquiry(PERSON_ID);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(ENQUIRY_ID, result.getId(), "Response ID should match saved enquiry ID");

        ArgumentCaptor<CreditBureauEnquiry> captor = ArgumentCaptor.forClass(CreditBureauEnquiry.class);
        verify(creditBureauRepositoryWrapper).saveWithException(captor.capture());
        assertEquals(CreditBureauEnquiryStatus.INITIATED, captor.getValue().getStatus(),
                "Enquiry should be saved with INITIATED status");
    }

    // ==================== linkConsentToEnquiry() Tests ====================

    @Test
    void linkConsentToEnquiry_setsConsentIdAndSaves() {
        // Arrange
        Long consentId = 50L;
        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(enquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(enquiry);

        // Act
        creditBureauWriteService.linkConsentToEnquiry(ENQUIRY_ID, consentId);

        // Assert
        assertEquals(consentId, enquiry.getConsentId(), "Consent ID should be set on enquiry");
        verify(creditBureauRepositoryWrapper).saveWithException(enquiry);
    }

    // ==================== executeCreditBureauFlowAsync() Tests ====================

    @Test
    void executeCreditBureauFlowAsync_withNullPersonData_completesExceptionally() {
        // Arrange
        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("error");

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, null);

        // Assert
        assertThrows(ExecutionException.class, future::get,
                "Future should complete exceptionally when personData is null");
    }

    @Test
    void executeCreditBureauFlowAsync_withSuccessResponse_updatesEnquiryAndReturnsResponse() throws Exception {
        // Arrange
        CreditBureauPersonData personData = CreditBureauPersonData.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        RunConfig runConfig = new RunConfig();
        ThirdPartyConfig primaryConfig = new ThirdPartyConfig();
        primaryConfig.setProvider(PROVIDER_NAME);
        runConfig.setPrimaryConfig(primaryConfig);

        CreditBureauHandler handler = mock(CreditBureauHandler.class);

        CreditBureauProviderResponse providerResponse = CreditBureauProviderResponse.builder()
                .status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.SUCCESS)
                .reportId("RPT-001")
                .stage1Request("{}")
                .creditBureauReportJson("{\"B2C-REPORT\":{}}")
                .reportDetails(Map.of("key", "value"))
                .build();

        when(thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.CREDIT_BUREAU))
                .thenReturn(runConfig);
        when(serviceFactory.getHandler(ThirdPartyServiceList.CREDIT_BUREAU)).thenReturn(handler);
        when(handler.pullEnquiry(any(PullEnquiryRequest.class), any(), eq(PROVIDER_NAME)))
                .thenReturn(providerResponse);

        CreditBureauEnquiry updatedEnquiry = new CreditBureauEnquiry();
        updatedEnquiry.setId(ENQUIRY_ID);
        updatedEnquiry.setIdentifier(enquiryIdentifier);
        updatedEnquiry.setStatus(CreditBureauEnquiryStatus.SUCCESS);
        updatedEnquiry.setReportId("RPT-001");

        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(updatedEnquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(updatedEnquiry);

        CreditBureauReportParser parser = mock(CreditBureauReportParser.class);
        when(creditBureauReportParserFactory.getParser(PROVIDER_NAME)).thenReturn(parser);
        when(parser.parse(eq(ENQUIRY_ID), any(JsonNode.class))).thenReturn(true);
        when(objectMapper.readTree(anyString())).thenReturn(mock(JsonNode.class));

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);
        CreditBureauEnquiryResponse result = future.get();

        // Assert
        assertNotNull(result, "Response should not be null on success");
        assertEquals(ENQUIRY_ID, result.getId(), "Response ID should match enquiry ID");
    }

    @Test
    void executeCreditBureauFlowAsync_whenHandlerThrowsException_setsFailedStatusAndThrows() {
        // Arrange
        CreditBureauPersonData personData = CreditBureauPersonData.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        RunConfig runConfig = new RunConfig();
        ThirdPartyConfig primaryConfig = new ThirdPartyConfig();
        primaryConfig.setProvider(PROVIDER_NAME);
        runConfig.setPrimaryConfig(primaryConfig);

        CreditBureauHandler handler = mock(CreditBureauHandler.class);

        when(thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.CREDIT_BUREAU))
                .thenReturn(runConfig);
        when(serviceFactory.getHandler(ThirdPartyServiceList.CREDIT_BUREAU)).thenReturn(handler);
        when(handler.pullEnquiry(any(PullEnquiryRequest.class), any(), eq(PROVIDER_NAME)))
                .thenThrow(new RuntimeException("Provider error"));

        CreditBureauEnquiry enquiryForUpdate = new CreditBureauEnquiry();
        enquiryForUpdate.setId(ENQUIRY_ID);
        enquiryForUpdate.setIdentifier(enquiryIdentifier);
        enquiryForUpdate.setStatus(CreditBureauEnquiryStatus.INITIATED);

        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(enquiryForUpdate);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(enquiryForUpdate);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("error");

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);

        // Assert
        assertThrows(ExecutionException.class, future::get,
                "Future should complete exceptionally when handler throws");
        assertEquals(CreditBureauEnquiryStatus.FAILED, enquiryForUpdate.getStatus(),
                "Enquiry status should be set to FAILED on error");
    }

    @Test
    void executeCreditBureauFlowAsync_whenNoHitDetected_updatesStatusToNoHit() throws Exception {
        // Arrange
        CreditBureauPersonData personData = CreditBureauPersonData.builder()
                .firstName("Jane")
                .build();

        RunConfig runConfig = new RunConfig();
        ThirdPartyConfig primaryConfig = new ThirdPartyConfig();
        primaryConfig.setProvider(PROVIDER_NAME);
        runConfig.setPrimaryConfig(primaryConfig);

        CreditBureauHandler handler = mock(CreditBureauHandler.class);

        CreditBureauProviderResponse providerResponse = CreditBureauProviderResponse.builder()
                .status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.SUCCESS)
                .reportId("RPT-002")
                .stage1Request("{}")
                .creditBureauReportJson("{\"B2C-REPORT\":{}}")
                .reportDetails(Map.of("key", "value"))
                .build();

        when(thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.CREDIT_BUREAU))
                .thenReturn(runConfig);
        when(serviceFactory.getHandler(ThirdPartyServiceList.CREDIT_BUREAU)).thenReturn(handler);
        when(handler.pullEnquiry(any(PullEnquiryRequest.class), any(), eq(PROVIDER_NAME)))
                .thenReturn(providerResponse);

        CreditBureauEnquiry updatedEnquiry = new CreditBureauEnquiry();
        updatedEnquiry.setId(ENQUIRY_ID);
        updatedEnquiry.setIdentifier(enquiryIdentifier);
        updatedEnquiry.setStatus(CreditBureauEnquiryStatus.SUCCESS);

        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(updatedEnquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(updatedEnquiry);

        CreditBureauReportParser parser = mock(CreditBureauReportParser.class);
        when(creditBureauReportParserFactory.getParser(PROVIDER_NAME)).thenReturn(parser);
        when(parser.parse(eq(ENQUIRY_ID), any(JsonNode.class))).thenReturn(false);
        when(objectMapper.readTree(anyString())).thenReturn(mock(JsonNode.class));

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);
        future.get();

        // Assert
        assertEquals(CreditBureauEnquiryStatus.NO_HIT, updatedEnquiry.getStatus(),
                "Status should be updated to NO_HIT when parser finds no data");
    }

    // ==================== updateEnquiryWithResult branch coverage ====================

    @Test
    void executeCreditBureauFlowAsync_withNullReportDetails_setsReportDetailsToNull() throws Exception {
        // Arrange — covers reportDetails == null branch
        CreditBureauPersonData personData = CreditBureauPersonData.builder().firstName("John").build();
        CreditBureauHandler handler = mock(CreditBureauHandler.class);
        RunConfig runConfig = buildRunConfig();

        CreditBureauProviderResponse providerResponse = CreditBureauProviderResponse.builder()
                .status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.FAILED)
                .reportId("RPT-003")
                .stage1Request("{}")
                .creditBureauReportJson("{}")
                .reportDetails(null)
                .build();

        stubAsyncFlowSetup(runConfig, handler, providerResponse);

        CreditBureauEnquiry updatedEnquiry = buildUpdatedEnquiry(CreditBureauEnquiryStatus.FAILED);
        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(updatedEnquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(updatedEnquiry);

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);
        future.get();

        // Assert
        assertNull(updatedEnquiry.getReportDetails(), "Report details should be null when response has null report details");
    }

    @Test
    void executeCreditBureauFlowAsync_withEmptyReportDetails_setsReportDetailsToNull() throws Exception {
        // Arrange — covers reportDetails.isEmpty() branch
        CreditBureauPersonData personData = CreditBureauPersonData.builder().firstName("John").build();
        CreditBureauHandler handler = mock(CreditBureauHandler.class);
        RunConfig runConfig = buildRunConfig();

        CreditBureauProviderResponse providerResponse = CreditBureauProviderResponse.builder()
                .status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.FAILED)
                .stage1Request("{}")
                .creditBureauReportJson("{}")
                .reportDetails(Collections.emptyMap())
                .build();

        stubAsyncFlowSetup(runConfig, handler, providerResponse);

        CreditBureauEnquiry updatedEnquiry = buildUpdatedEnquiry(CreditBureauEnquiryStatus.FAILED);
        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(updatedEnquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(updatedEnquiry);

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);
        future.get();

        // Assert
        assertNull(updatedEnquiry.getReportDetails(), "Report details should be null when response has empty map");
    }

    @Test
    void executeCreditBureauFlowAsync_withNullStage1Request_setsEmptyRequestJson() throws Exception {
        // Arrange — covers stage1Request == null ternary branch
        CreditBureauPersonData personData = CreditBureauPersonData.builder().firstName("John").build();
        CreditBureauHandler handler = mock(CreditBureauHandler.class);
        RunConfig runConfig = buildRunConfig();

        CreditBureauProviderResponse providerResponse = CreditBureauProviderResponse.builder()
                .status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.FAILED)
                .stage1Request(null)
                .creditBureauReportJson(null)
                .build();

        stubAsyncFlowSetup(runConfig, handler, providerResponse);

        CreditBureauEnquiry updatedEnquiry = buildUpdatedEnquiry(CreditBureauEnquiryStatus.FAILED);
        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(updatedEnquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(updatedEnquiry);

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);
        future.get();

        // Assert
        assertEquals("", updatedEnquiry.getRequestJson(), "Request JSON should be empty when stage1Request is null");
        assertEquals("", updatedEnquiry.getResponseJson(), "Response JSON should be empty when creditBureauReportJson is null");
    }

    @Test
    void executeCreditBureauFlowAsync_withBlankStage1Request_setsEmptyRequestJson() throws Exception {
        // Arrange — covers stage1Request.trim().isEmpty() ternary branch
        CreditBureauPersonData personData = CreditBureauPersonData.builder().firstName("John").build();
        CreditBureauHandler handler = mock(CreditBureauHandler.class);
        RunConfig runConfig = buildRunConfig();

        CreditBureauProviderResponse providerResponse = CreditBureauProviderResponse.builder()
                .status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.FAILED)
                .stage1Request("   ")
                .creditBureauReportJson("   ")
                .build();

        stubAsyncFlowSetup(runConfig, handler, providerResponse);

        CreditBureauEnquiry updatedEnquiry = buildUpdatedEnquiry(CreditBureauEnquiryStatus.FAILED);
        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(updatedEnquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(updatedEnquiry);

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);
        future.get();

        // Assert
        assertEquals("", updatedEnquiry.getRequestJson(), "Request JSON should be empty when stage1Request is blank");
        assertEquals("", updatedEnquiry.getResponseJson(), "Response JSON should be empty when creditBureauReportJson is blank");
    }

    @Test
    void executeCreditBureauFlowAsync_withNonSuccessStatus_skipsReportParsing() throws Exception {
        // Arrange — covers CreditBureauEnquiryStatus.SUCCESS != enquiry.getStatus() branch
        CreditBureauPersonData personData = CreditBureauPersonData.builder().firstName("John").build();
        CreditBureauHandler handler = mock(CreditBureauHandler.class);
        RunConfig runConfig = buildRunConfig();

        CreditBureauProviderResponse providerResponse = CreditBureauProviderResponse.builder()
                .status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.FAILED)
                .stage1Request("{}")
                .creditBureauReportJson("{\"data\":\"something\"}")
                .build();

        stubAsyncFlowSetup(runConfig, handler, providerResponse);

        CreditBureauEnquiry updatedEnquiry = buildUpdatedEnquiry(CreditBureauEnquiryStatus.FAILED);
        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(updatedEnquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(updatedEnquiry);

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);
        future.get();

        // Assert
        verifyNoInteractions(creditBureauReportParserFactory);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void executeCreditBureauFlowAsync_withSuccessButEmptyReportJson_skipsReportParsing() throws Exception {
        // Arrange — covers SUCCESS status but creditBureauReportJson.trim().isEmpty() branch
        CreditBureauPersonData personData = CreditBureauPersonData.builder().firstName("John").build();
        CreditBureauHandler handler = mock(CreditBureauHandler.class);
        RunConfig runConfig = buildRunConfig();

        CreditBureauProviderResponse providerResponse = CreditBureauProviderResponse.builder()
                .status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.SUCCESS)
                .stage1Request("{}")
                .creditBureauReportJson("")
                .reportDetails(Map.of("key", "value"))
                .build();

        stubAsyncFlowSetup(runConfig, handler, providerResponse);

        CreditBureauEnquiry updatedEnquiry = buildUpdatedEnquiry(CreditBureauEnquiryStatus.SUCCESS);
        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(updatedEnquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(updatedEnquiry);

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);
        future.get();

        // Assert
        verifyNoInteractions(creditBureauReportParserFactory);
    }

    @Test
    void executeCreditBureauFlowAsync_whenProviderConfigThrowsInUpdate_continuesWithoutProvider() throws Exception {
        // Arrange — covers exception in provider name fetch inside updateEnquiryWithResult
        CreditBureauPersonData personData = CreditBureauPersonData.builder().firstName("John").build();
        CreditBureauHandler handler = mock(CreditBureauHandler.class);
        RunConfig runConfig = buildRunConfig();

        CreditBureauProviderResponse providerResponse = CreditBureauProviderResponse.builder()
                .status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.FAILED)
                .stage1Request("{}")
                .creditBureauReportJson("{}")
                .build();

        when(serviceFactory.getHandler(ThirdPartyServiceList.CREDIT_BUREAU)).thenReturn(handler);
        when(handler.pullEnquiry(any(PullEnquiryRequest.class), any(), eq(PROVIDER_NAME)))
                .thenReturn(providerResponse);
        // First call in executeCreditBureauFlowAsync succeeds, second call in updateEnquiryWithResult throws
        when(thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.CREDIT_BUREAU))
                .thenReturn(runConfig)
                .thenThrow(new RuntimeException("Config unavailable"));

        CreditBureauEnquiry updatedEnquiry = buildUpdatedEnquiry(CreditBureauEnquiryStatus.FAILED);
        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(updatedEnquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(updatedEnquiry);

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);
        future.get();

        // Assert — should complete without exception despite provider config failure in update
        assertNull(updatedEnquiry.getProvider(), "Provider should remain null when config fetch fails in update");
    }

    @Test
    void executeCreditBureauFlowAsync_whenParserThrowsException_continuesWithoutFailingFlow() throws Exception {
        // Arrange — covers exception in report parsing catch block
        CreditBureauPersonData personData = CreditBureauPersonData.builder().firstName("John").build();
        CreditBureauHandler handler = mock(CreditBureauHandler.class);
        RunConfig runConfig = buildRunConfig();

        CreditBureauProviderResponse providerResponse = CreditBureauProviderResponse.builder()
                .status(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus.SUCCESS)
                .stage1Request("{}")
                .creditBureauReportJson("{\"B2C-REPORT\":{}}")
                .reportDetails(Map.of("key", "value"))
                .build();

        stubAsyncFlowSetup(runConfig, handler, providerResponse);

        CreditBureauEnquiry updatedEnquiry = buildUpdatedEnquiry(CreditBureauEnquiryStatus.SUCCESS);
        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID)).thenReturn(updatedEnquiry);
        when(creditBureauRepositoryWrapper.saveWithException(any(CreditBureauEnquiry.class))).thenReturn(updatedEnquiry);

        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("Parse error"));

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);
        CreditBureauEnquiryResponse result = future.get();

        // Assert — flow completes despite parse error
        assertNotNull(result, "Response should not be null even when parser fails");
        assertEquals(CreditBureauEnquiryStatus.SUCCESS, updatedEnquiry.getStatus(),
                "Status should remain SUCCESS when parser fails (error is logged, not thrown)");
    }

    @Test
    void executeCreditBureauFlowAsync_whenSaveFailedStatusThrows_stillThrowsOriginalException() {
        // Arrange — covers inner save exception in catch block
        CreditBureauPersonData personData = CreditBureauPersonData.builder().firstName("John").build();
        CreditBureauHandler handler = mock(CreditBureauHandler.class);
        RunConfig runConfig = buildRunConfig();

        when(thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.CREDIT_BUREAU))
                .thenReturn(runConfig);
        when(serviceFactory.getHandler(ThirdPartyServiceList.CREDIT_BUREAU)).thenReturn(handler);
        when(handler.pullEnquiry(any(PullEnquiryRequest.class), any(), eq(PROVIDER_NAME)))
                .thenThrow(new RuntimeException("Provider error"));

        when(creditBureauRepositoryWrapper.findByIdWithException(ENQUIRY_ID))
                .thenThrow(new RuntimeException("DB unavailable"));
        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("error");

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, PERSON_ID, personData);

        // Assert — original exception still propagates
        assertThrows(ExecutionException.class, future::get,
                "Future should complete exceptionally even when save-failed-status also throws");
    }

    @Test
    void executeCreditBureauFlowAsync_withNullEnquiryId_skipsSaveFailedStatus() {
        // Arrange — covers enquiryId == null branch in catch block
        CreditBureauEnquiry nullIdEnquiry = new CreditBureauEnquiry();
        nullIdEnquiry.setId(null);
        nullIdEnquiry.setIdentifier(enquiryIdentifier);

        lenient().when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("error");

        // Act
        CompletableFuture<CreditBureauEnquiryResponse> future =
                creditBureauWriteService.executeCreditBureauFlowAsync(nullIdEnquiry, PERSON_ID, null);

        // Assert
        assertThrows(ExecutionException.class, future::get,
                "Future should complete exceptionally");
        verify(creditBureauRepositoryWrapper, never()).findByIdWithException(any());
    }

    // ==================== Helpers ====================

    private RunConfig buildRunConfig() {
        RunConfig runConfig = new RunConfig();
        ThirdPartyConfig primaryConfig = new ThirdPartyConfig();
        primaryConfig.setProvider(PROVIDER_NAME);
        runConfig.setPrimaryConfig(primaryConfig);
        return runConfig;
    }

    private CreditBureauEnquiry buildUpdatedEnquiry(CreditBureauEnquiryStatus status) {
        CreditBureauEnquiry updated = new CreditBureauEnquiry();
        updated.setId(ENQUIRY_ID);
        updated.setIdentifier(enquiryIdentifier);
        updated.setStatus(status);
        return updated;
    }

    private void stubAsyncFlowSetup(RunConfig runConfig, CreditBureauHandler handler,
                                     CreditBureauProviderResponse providerResponse) {
        when(thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.CREDIT_BUREAU))
                .thenReturn(runConfig);
        when(serviceFactory.getHandler(ThirdPartyServiceList.CREDIT_BUREAU)).thenReturn(handler);
        when(handler.pullEnquiry(any(PullEnquiryRequest.class), any(), eq(PROVIDER_NAME)))
                .thenReturn(providerResponse);
    }
}
