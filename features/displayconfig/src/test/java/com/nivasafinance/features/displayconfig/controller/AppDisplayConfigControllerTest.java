package com.nivasafinance.features.displayconfig.controller;

import com.nivasafinance.features.displayconfig.dto.AppDisplayConfigResponse;
import com.nivasafinance.features.displayconfig.enums.AppType;
import com.nivasafinance.features.displayconfig.service.AppDisplayConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppDisplayConfigControllerTest {

    @Mock
    private AppDisplayConfigService appDisplayConfigService;

    @InjectMocks
    private AppDisplayConfigController controller;

    @Test
    void getByAppType_success_returnsOk() {
        AppDisplayConfigResponse response = AppDisplayConfigResponse.builder()
                .appType(AppType.CRM)
                .config(Map.of("theme", "dark"))
                .build();
        when(appDisplayConfigService.getByAppType(AppType.CRM)).thenReturn(response);

        ResponseEntity<AppDisplayConfigResponse> result = controller.getByAppType(AppType.CRM);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(AppType.CRM, result.getBody().getAppType());
        verify(appDisplayConfigService).getByAppType(AppType.CRM);
    }

    @Test
    void getByAppType_serviceThrows_propagatesException() {
        when(appDisplayConfigService.getByAppType(AppType.CUSTOMER))
                .thenThrow(new RuntimeException("Not found"));

        assertThrows(RuntimeException.class,
                () -> controller.getByAppType(AppType.CUSTOMER));
    }
}
