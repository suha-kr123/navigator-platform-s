package com.nivasafinance.features.displayconfig.service.impl;

import com.nivasafinance.features.displayconfig.dto.AppDisplayConfigResponse;
import com.nivasafinance.features.displayconfig.entity.AppDisplayConfig;
import com.nivasafinance.features.displayconfig.enums.AppType;
import com.nivasafinance.features.displayconfig.repository.AppDisplayConfigRepositoryWrapper;
import com.nivasafinance.features.displayconfig.service.AppDisplayConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppDisplayConfigServiceImpl implements AppDisplayConfigService {

    private final AppDisplayConfigRepositoryWrapper repositoryWrapper;

    @Override
    @Transactional(readOnly = true)
    public AppDisplayConfigResponse getByAppType(AppType appType) {
        AppDisplayConfig config = repositoryWrapper.findByAppTypeWithException(appType);
        return AppDisplayConfigResponse.from(config);
    }
}
