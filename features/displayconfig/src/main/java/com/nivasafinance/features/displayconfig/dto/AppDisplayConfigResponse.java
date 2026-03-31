package com.nivasafinance.features.displayconfig.dto;

import com.nivasafinance.features.displayconfig.entity.AppDisplayConfig;
import com.nivasafinance.features.displayconfig.enums.AppType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDisplayConfigResponse {

    private AppType appType;
    private Map<String, Object> config;

    public static AppDisplayConfigResponse from(AppDisplayConfig entity) {
        return AppDisplayConfigResponse.builder()
                .appType(entity.getAppType())
                .config(entity.getConfig())
                .build();
    }
}
