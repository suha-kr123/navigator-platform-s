package com.nivasafinance.features.displayconfig.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.displayconfig.dto.AppDisplayConfigResponse;
import com.nivasafinance.features.displayconfig.enums.AppType;
import com.nivasafinance.features.displayconfig.service.AppDisplayConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/display-config")
@RequiredArgsConstructor
public class AppDisplayConfigController {

    private final AppDisplayConfigService appDisplayConfigService;

    @GetMapping
    @RequirePermission(permissionName = "READ_DISPLAY_CONFIG")
    public ResponseEntity<AppDisplayConfigResponse> getByAppType(@RequestParam AppType appType) {
        return ResponseEntity.ok(appDisplayConfigService.getByAppType(appType));
    }
}
