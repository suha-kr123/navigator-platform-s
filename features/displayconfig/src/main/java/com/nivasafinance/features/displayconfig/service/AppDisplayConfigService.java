package com.nivasafinance.features.displayconfig.service;

import com.nivasafinance.features.displayconfig.dto.AppDisplayConfigResponse;
import com.nivasafinance.features.displayconfig.enums.AppType;

public interface AppDisplayConfigService {

    AppDisplayConfigResponse getByAppType(AppType appType);
}
