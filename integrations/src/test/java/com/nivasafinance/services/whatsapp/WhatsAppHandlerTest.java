package com.nivasafinance.services.whatsapp;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.data.RunConfig;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.exception.ServiceInvocationException;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.WhatsAppProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppHandlerTest {

    @Mock
    private WhatsAppProvider<?> watiProvider;

    @Mock
    private WhatsAppProvider<?> gallaboxProvider;

    private WhatsAppHandler handler;

    @BeforeEach
    void setUp() {
        lenient().when(watiProvider.getKey()).thenReturn(ThirdPartyProviderList.WATI);
        lenient().when(gallaboxProvider.getKey()).thenReturn(ThirdPartyProviderList.GALLABOX);
        handler = new WhatsAppHandler(Set.of(watiProvider, gallaboxProvider));
    }

    @Test
    void getKey_returnsWhatsApp() {
        assertEquals(ThirdPartyServiceList.WHATSAPP, handler.getKey(), "Key should be WHATSAPP");
    }

    @Test
    void constructor_registersProvidersByName() {
        @SuppressWarnings("unchecked")
        Map<String, WhatsAppProvider<?>> servicesMap =
                (Map<String, WhatsAppProvider<?>>) ReflectionTestUtils.getField(handler, "servicesMap");

        assertNotNull(servicesMap, "Services map should not be null");
        assertEquals(2, servicesMap.size(), "Should register both providers");
        assertSame(watiProvider, servicesMap.get("wati"), "WATI provider should be registered");
        assertSame(gallaboxProvider, servicesMap.get("gallabox"), "Gallabox provider should be registered");
    }

    @Test
    void sendTemplate_nullPrimaryProvider_throwsServiceInvocationException() {
        ThirdPartyConfig primaryConfig = new ThirdPartyConfig();
        primaryConfig.setProvider("unknown_provider");
        RunConfig runConfig = new RunConfig();
        runConfig.setPrimaryConfig(primaryConfig);
        ReflectionTestUtils.setField(handler, "runConfig", runConfig);

        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("9876543210")
                .templateName("test_template")
                .build();

        assertThrows(ServiceInvocationException.class,
                () -> handler.sendTemplate(request, new BusinessContext()),
                "Should throw when primary provider not found");
    }

    @Test
    void getTemplateStatus_nullPrimaryProvider_throwsServiceInvocationException() {
        ThirdPartyConfig primaryConfig = new ThirdPartyConfig();
        primaryConfig.setProvider("unknown_provider");
        RunConfig runConfig = new RunConfig();
        runConfig.setPrimaryConfig(primaryConfig);
        ReflectionTestUtils.setField(handler, "runConfig", runConfig);

        assertThrows(ServiceInvocationException.class,
                () -> handler.getTemplateStatus("9876543210", new BusinessContext()),
                "Should throw when primary provider not found");
    }
}
