package com.nivasafinance.features.displayconfig.repository;

import com.nivasafinance.features.displayconfig.entity.AppDisplayConfig;
import com.nivasafinance.features.displayconfig.enums.AppType;
import com.nivasafinance.features.displayconfig.exception.DisplayConfigNotFoundException;
import com.nivasafinance.features.displayconfig.exception.DisplayConfigOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppDisplayConfigRepositoryWrapperTest {

    @Mock
    private AppDisplayConfigRepository appDisplayConfigRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AppDisplayConfigRepositoryWrapper repositoryWrapper;

    @BeforeEach
    void setUp() {
        lenient().when(messageSource.getMessage(any(), any(), any())).thenReturn("Error");
    }

    @Test
    void findByAppTypeWithException_found_returnsConfig() {
        AppDisplayConfig config = AppDisplayConfig.builder()
                .appType(AppType.CRM)
                .config(Map.of("theme", "dark"))
                .isActive(true)
                .build();
        when(appDisplayConfigRepository.findByAppTypeAndIsActiveTrue(AppType.CRM))
                .thenReturn(Optional.of(config));

        AppDisplayConfig result = repositoryWrapper.findByAppTypeWithException(AppType.CRM);

        assertNotNull(result);
        assertEquals(AppType.CRM, result.getAppType());
    }

    @Test
    void findByAppTypeWithException_notFound_throwsDisplayConfigNotFoundException() {
        when(appDisplayConfigRepository.findByAppTypeAndIsActiveTrue(AppType.ADMIN))
                .thenReturn(Optional.empty());

        assertThrows(DisplayConfigNotFoundException.class,
                () -> repositoryWrapper.findByAppTypeWithException(AppType.ADMIN));
    }

    @Test
    void findByAppTypeWithException_runtimeException_throwsDisplayConfigOperationException() {
        when(appDisplayConfigRepository.findByAppTypeAndIsActiveTrue(AppType.CRM))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DisplayConfigOperationException.class,
                () -> repositoryWrapper.findByAppTypeWithException(AppType.CRM));
    }
}
