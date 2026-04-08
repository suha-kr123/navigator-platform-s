package com.nivasafinance.features.displayconfig.service.impl;

import com.nivasafinance.features.displayconfig.dto.AppDisplayConfigResponse;
import com.nivasafinance.features.displayconfig.entity.AppDisplayConfig;
import com.nivasafinance.features.displayconfig.enums.AppType;
import com.nivasafinance.features.displayconfig.repository.AppDisplayConfigRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppDisplayConfigServiceImplTest {

    @Mock
    private AppDisplayConfigRepositoryWrapper repositoryWrapper;

    @InjectMocks
    private AppDisplayConfigServiceImpl appDisplayConfigService;

    @Test
    void getByAppType_success_returnsMappedResponse() {
        AppDisplayConfig config = AppDisplayConfig.builder()
                .appType(AppType.CRM)
                .config(Map.of("theme", "dark", "language", "en"))
                .isActive(true)
                .build();

        when(repositoryWrapper.findByAppTypeWithException(AppType.CRM)).thenReturn(config);

        AppDisplayConfigResponse result = appDisplayConfigService.getByAppType(AppType.CRM);

        assertNotNull(result);
        assertEquals(AppType.CRM, result.getAppType());
        assertEquals(Map.of("theme", "dark", "language", "en"), result.getConfig());
        verify(repositoryWrapper).findByAppTypeWithException(AppType.CRM);
    }

    @Test
    void getByAppType_delegatesToRepositoryWrapper() {
        AppDisplayConfig config = AppDisplayConfig.builder()
                .appType(AppType.ADMIN)
                .config(Map.of("key", "value"))
                .isActive(true)
                .build();

        when(repositoryWrapper.findByAppTypeWithException(AppType.ADMIN)).thenReturn(config);

        AppDisplayConfigResponse result = appDisplayConfigService.getByAppType(AppType.ADMIN);

        assertEquals(AppType.ADMIN, result.getAppType());
        verify(repositoryWrapper).findByAppTypeWithException(AppType.ADMIN);
    }

    @Test
    void getByAppType_repositoryThrowsException_propagatesException() {
        when(repositoryWrapper.findByAppTypeWithException(AppType.CUSTOMER))
                .thenThrow(new RuntimeException("Config not found"));

        assertThrows(RuntimeException.class,
                () -> appDisplayConfigService.getByAppType(AppType.CUSTOMER));
    }

    @ParameterizedTest
    @EnumSource(AppType.class)
    void getByAppType_allAppTypes_delegatesToWrapper(AppType appType) {
        AppDisplayConfig config = AppDisplayConfig.builder()
                .appType(appType)
                .config(Map.of("key", "value"))
                .isActive(true)
                .build();

        when(repositoryWrapper.findByAppTypeWithException(appType)).thenReturn(config);

        AppDisplayConfigResponse result = appDisplayConfigService.getByAppType(appType);

        assertEquals(appType, result.getAppType());
    }

    @Test
    void getByAppType_configWithEmptyMap_returnsEmptyConfig() {
        AppDisplayConfig config = AppDisplayConfig.builder()
                .appType(AppType.FIELD_AGENT)
                .config(Map.of())
                .isActive(true)
                .build();

        when(repositoryWrapper.findByAppTypeWithException(AppType.FIELD_AGENT)).thenReturn(config);

        AppDisplayConfigResponse result = appDisplayConfigService.getByAppType(AppType.FIELD_AGENT);

        assertNotNull(result.getConfig());
        assertTrue(result.getConfig().isEmpty());
    }
}
