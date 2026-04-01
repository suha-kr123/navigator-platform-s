package com.nivasafinance.features.displayconfig.repository;

import com.nivasafinance.features.displayconfig.entity.AppDisplayConfig;
import com.nivasafinance.features.displayconfig.enums.AppType;
import com.nivasafinance.features.displayconfig.exception.DisplayConfigExceptionFactory;
import com.nivasafinance.features.displayconfig.exception.DisplayConfigNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppDisplayConfigRepositoryWrapper {

    private final AppDisplayConfigRepository appDisplayConfigRepository;
    private final MessageSource messageSource;

    public AppDisplayConfig findByAppTypeWithException(AppType appType) {
        try {
            return appDisplayConfigRepository.findByAppTypeAndIsActiveTrue(appType)
                    .orElseThrow(() -> DisplayConfigExceptionFactory.notFound(appType.name(), messageSource));
        } catch (DisplayConfigNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw DisplayConfigExceptionFactory.retrieveFailed(messageSource);
        }
    }
}
