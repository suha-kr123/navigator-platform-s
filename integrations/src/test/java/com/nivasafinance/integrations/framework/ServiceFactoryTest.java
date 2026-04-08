package com.nivasafinance.integrations.framework;

import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.data.RunConfig;
import com.nivasafinance.integrations.framework.core.exception.ServiceFactoryException;
import com.nivasafinance.integrations.framework.core.service.ThirdPartyServiceConfigReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceFactoryTest {

    @Mock
    private ThirdPartyServiceConfigReadService thirdPartyServiceConfigReadService;

    @Mock
    private ThirdPartyHandler voiceHandler;

    private ServiceFactory<ThirdPartyHandler> serviceFactory;

    @BeforeEach
    void setUp() {
        when(voiceHandler.getKey()).thenReturn(ThirdPartyServiceList.VOICE);
        serviceFactory = new ServiceFactory<>(Set.of(voiceHandler), thirdPartyServiceConfigReadService);
    }

    // ── getHandler ──

    @Test
    void getHandler_whenHandlerExists_returnsConfiguredHandler() {
        RunConfig runConfig = new RunConfig();
        when(thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.VOICE)).thenReturn(runConfig);

        ThirdPartyHandler result = serviceFactory.getHandler(ThirdPartyServiceList.VOICE);

        assertSame(voiceHandler, result, "Should return the registered handler for the given service");
        verify(voiceHandler).setupConfig(runConfig);
    }

    @Test
    void getHandler_whenHandlerNotFound_throwsServiceFactoryException() {
        RunConfig runConfig = new RunConfig();
        when(thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.WHATSAPP)).thenReturn(runConfig);

        assertThrows(ServiceFactoryException.class,
                () -> serviceFactory.getHandler(ThirdPartyServiceList.WHATSAPP),
                "Should throw ServiceFactoryException when no handler is registered for the service");
    }

    @Test
    void getHandler_withMultipleHandlers_returnsCorrectHandler() {
        ThirdPartyHandler whatsappHandler = mock(ThirdPartyHandler.class);
        when(whatsappHandler.getKey()).thenReturn(ThirdPartyServiceList.WHATSAPP);

        ServiceFactory<ThirdPartyHandler> factory = new ServiceFactory<>(
                Set.of(voiceHandler, whatsappHandler), thirdPartyServiceConfigReadService);

        RunConfig runConfig = new RunConfig();
        when(thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.WHATSAPP)).thenReturn(runConfig);

        ThirdPartyHandler result = factory.getHandler(ThirdPartyServiceList.WHATSAPP);

        assertSame(whatsappHandler, result, "Should return the correct handler when multiple handlers are registered");
    }
}
