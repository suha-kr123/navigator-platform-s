package com.nivasafinance.integrations.framework.core.service;

import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.data.RunConfig;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.entity.ThirdPartyServiceConfig;
import com.nivasafinance.integrations.framework.core.exception.ServiceConfigurationException;
import com.nivasafinance.integrations.framework.core.repository.ThirdPartyServiceConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThirdPartyServiceConfigReadServiceTest {

    @Mock
    private ThirdPartyServiceConfigRepository thirdPartyServiceConfigRepository;

    @Mock
    private ThirdPartyProviderConfigReadService thirdPartyProviderConfigReadService;

    @InjectMocks
    private ThirdPartyServiceConfigReadService service;

    // ── findByService ──

    @Test
    void findByService_whenServiceExists_returnsRunConfigWithPrimaryAndFallback() {
        ThirdPartyServiceConfig serviceConfig = new ThirdPartyServiceConfig();
        serviceConfig.setPrimaryConfigId(10L);
        serviceConfig.setFallbackConfigId(20L);
        serviceConfig.setRetryCount(3);

        ThirdPartyConfig primaryConfig = new ThirdPartyConfig(10L, "primary", "provider", Map.of());
        ThirdPartyConfig fallbackConfig = new ThirdPartyConfig(20L, "fallback", "provider", Map.of());

        when(thirdPartyServiceConfigRepository.findByServiceAndIsActiveTrue("voice"))
                .thenReturn(Optional.of(serviceConfig));
        when(thirdPartyProviderConfigReadService.getProviderConfigById(10L)).thenReturn(primaryConfig);
        when(thirdPartyProviderConfigReadService.getProviderConfigById(20L)).thenReturn(fallbackConfig);

        RunConfig result = service.findByService(ThirdPartyServiceList.VOICE);

        assertSame(primaryConfig, result.getPrimaryConfig(),
                "RunConfig should contain the primary provider config");
        assertSame(fallbackConfig, result.getFallbackConfig(),
                "RunConfig should contain the fallback provider config");
        assertEquals(3, result.getRetries(),
                "RunConfig retry count should match the service config");
    }

    @Test
    void findByService_whenFallbackConfigIdIsNull_returnsRunConfigWithNullFallback() {
        ThirdPartyServiceConfig serviceConfig = new ThirdPartyServiceConfig();
        serviceConfig.setPrimaryConfigId(10L);
        serviceConfig.setFallbackConfigId(null);
        serviceConfig.setRetryCount(1);

        ThirdPartyConfig primaryConfig = new ThirdPartyConfig(10L, "primary", "provider", Map.of());

        when(thirdPartyServiceConfigRepository.findByServiceAndIsActiveTrue("voice"))
                .thenReturn(Optional.of(serviceConfig));
        when(thirdPartyProviderConfigReadService.getProviderConfigById(10L)).thenReturn(primaryConfig);

        RunConfig result = service.findByService(ThirdPartyServiceList.VOICE);

        assertSame(primaryConfig, result.getPrimaryConfig(),
                "RunConfig should contain the primary provider config");
        assertNull(result.getFallbackConfig(),
                "Fallback config should be null when fallbackConfigId is not set");
        verify(thirdPartyProviderConfigReadService, never()).getProviderConfigById(20L);
    }

    @Test
    void findByService_whenServiceNotFound_throwsServiceConfigurationException() {
        when(thirdPartyServiceConfigRepository.findByServiceAndIsActiveTrue("voice"))
                .thenReturn(Optional.empty());

        assertThrows(ServiceConfigurationException.class,
                () -> service.findByService(ThirdPartyServiceList.VOICE),
                "Should throw ServiceConfigurationException when no active service config is found");
    }
}
