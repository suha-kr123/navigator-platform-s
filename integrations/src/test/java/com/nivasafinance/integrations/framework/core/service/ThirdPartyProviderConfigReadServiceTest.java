package com.nivasafinance.integrations.framework.core.service;

import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.entity.ThirdPartyProviderConfig;
import com.nivasafinance.integrations.framework.core.exception.ServiceConfigurationException;
import com.nivasafinance.integrations.framework.core.repository.ThirdPartyProviderConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThirdPartyProviderConfigReadServiceTest {

    @Mock
    private ThirdPartyProviderConfigRepository thirdPartyProviderConfigRepository;

    @InjectMocks
    private ThirdPartyProviderConfigReadService service;

    // ── getProviderConfigById ──

    @Test
    void getProviderConfigById_whenConfigExistsAndActive_returnsThirdPartyConfig() {
        ThirdPartyProviderConfig entity = new ThirdPartyProviderConfig();
        entity.setId(1L);
        entity.setName("exotel-config");
        entity.setProvider("exotel");
        entity.setConfigs("{\"apiKey\":\"key123\",\"secret\":\"sec456\"}");
        entity.setActive(true);

        when(thirdPartyProviderConfigRepository.findById(1L)).thenReturn(Optional.of(entity));

        ThirdPartyConfig result = service.getProviderConfigById(1L);

        assertEquals(1L, result.getId(), "Config id should match the entity id");
        assertEquals("exotel-config", result.getName(), "Config name should match the entity name");
        assertEquals("exotel", result.getProvider(), "Provider should match the entity provider");
        assertEquals("key123", result.getConfigurations().get("apiKey"),
                "Configurations map should contain the parsed apiKey");
        assertEquals("sec456", result.getConfigurations().get("secret"),
                "Configurations map should contain the parsed secret");
    }

    @Test
    void getProviderConfigById_whenConfigNotFound_throwsServiceConfigurationException() {
        when(thirdPartyProviderConfigRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ServiceConfigurationException.class,
                () -> service.getProviderConfigById(99L),
                "Should throw ServiceConfigurationException when config is not found");
    }

    @Test
    void getProviderConfigById_whenConfigNotActive_throwsServiceConfigurationException() {
        ThirdPartyProviderConfig entity = new ThirdPartyProviderConfig();
        entity.setId(1L);
        entity.setActive(false);

        when(thirdPartyProviderConfigRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThrows(ServiceConfigurationException.class,
                () -> service.getProviderConfigById(1L),
                "Should throw ServiceConfigurationException when config is not active");
    }
}
