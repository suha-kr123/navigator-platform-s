package com.nivasafinance.integrations.framework.core.logger;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ApiContext;
import com.nivasafinance.integrations.framework.core.entity.ThirdPartyResponseLog;
import com.nivasafinance.integrations.framework.core.repository.ThirdPartyResponseLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThirdPartyRequestResponseLoggerImplTest {

    @Mock
    private ThirdPartyResponseLogRepository thirdPartyResponseLogRepository;

    @InjectMocks
    private ThirdPartyRequestResponseLoggerImpl logger;

    // ── registerRequest ──

    @Test
    void registerRequest_savesLogEntryAndReturnsId() {
        BusinessContext businessContext = new BusinessContext("LOAN", 100L, "CREDIT_CHECK");
        ApiContext apiContext = new ApiContext("exotel", 5L);

        ThirdPartyResponseLog savedLog = new ThirdPartyResponseLog();
        savedLog.setId(42L);
        when(thirdPartyResponseLogRepository.save(any(ThirdPartyResponseLog.class))).thenReturn(savedLog);

        Long result = logger.registerRequest(businessContext, apiContext, HttpMethod.POST,
                "http://api.example.com/call", "{\"phone\":\"123\"}");

        assertEquals(42L, result, "Should return the id of the saved log entry");

        ArgumentCaptor<ThirdPartyResponseLog> captor = ArgumentCaptor.forClass(ThirdPartyResponseLog.class);
        verify(thirdPartyResponseLogRepository).save(captor.capture());

        ThirdPartyResponseLog captured = captor.getValue();
        assertEquals("LOAN", captured.getEntityType(), "Entity type should match business context");
        assertEquals(100L, captured.getEntityId(), "Entity id should match business context");
        assertEquals("POST", captured.getRequestMethod(), "Request method should be POST");
        assertEquals("http://api.example.com/call", captured.getUrl(), "URL should match the provided URL");
        assertEquals("{\"phone\":\"123\"}", captured.getRequest(), "Request body should match");
        assertEquals("CREDIT_CHECK", captured.getBusinessPurpose(), "Business purpose should match");
        assertEquals("exotel", captured.getProviderName(), "Provider name should match api context");
        assertEquals("5", captured.getProviderRefId(), "Provider ref id should match api context config id");
    }

    // ── registerThirdPartyRequest ──

    @Test
    void registerThirdPartyRequest_delegatesToRegisterRequest() {
        BusinessContext businessContext = new BusinessContext("LOAN", 100L, "CREDIT_CHECK");
        ApiContext apiContext = new ApiContext("exotel", 5L);

        ThirdPartyResponseLog savedLog = new ThirdPartyResponseLog();
        savedLog.setId(10L);
        when(thirdPartyResponseLogRepository.save(any(ThirdPartyResponseLog.class))).thenReturn(savedLog);

        Long result = logger.registerThirdPartyRequest(businessContext, apiContext, HttpMethod.GET,
                "http://api.example.com/status", null);

        assertEquals(10L, result, "Should return the id from the delegated registerRequest call");
        verify(thirdPartyResponseLogRepository).save(any(ThirdPartyResponseLog.class));
    }

    // ── registerResponse ──

    @Test
    void registerResponse_whenLogEntryExists_updatesResponseFields() {
        ThirdPartyResponseLog existingLog = new ThirdPartyResponseLog();
        existingLog.setId(42L);
        when(thirdPartyResponseLogRepository.findById(42L)).thenReturn(Optional.of(existingLog));

        logger.registerResponse(42L, "{\"status\":\"ok\"}", 150L, 200);

        assertEquals("{\"status\":\"ok\"}", existingLog.getResponse(),
                "Response body should be updated on the log entry");
        assertEquals(150L, existingLog.getResponseTimeInMs(),
                "Response time should be updated on the log entry");
        assertEquals(200, existingLog.getHttpStatusCode(),
                "HTTP status code should be updated on the log entry");
        verify(thirdPartyResponseLogRepository).save(existingLog);
    }

    @Test
    void registerResponse_whenLogEntryNotFound_doesNotSave() {
        when(thirdPartyResponseLogRepository.findById(99L)).thenReturn(Optional.empty());

        logger.registerResponse(99L, "body", 100L, 200);

        verify(thirdPartyResponseLogRepository, never()).save(any());
    }
}
