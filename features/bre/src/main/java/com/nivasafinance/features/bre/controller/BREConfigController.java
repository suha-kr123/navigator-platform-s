package com.nivasafinance.features.bre.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.bre.dto.BREConfigDetailedResponse;
import com.nivasafinance.features.bre.dto.BREConfigResponse;
import com.nivasafinance.features.bre.dto.BREConfigRuleFileUploadResponse;
import com.nivasafinance.features.bre.dto.CreateBREConfigRequest;
import com.nivasafinance.features.bre.service.BREConfigReadService;
import com.nivasafinance.features.bre.service.BREConfigWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiConstants.V1 + "/bre-configs")
@RequiredArgsConstructor
public class BREConfigController {

    private final BREConfigReadService breConfigReadService;
    private final BREConfigWriteService breConfigWriteService;

    @PostMapping
    @RequirePermission(permissionName = "CREATE_BRE_CONFIG")
    public ResponseEntity<BREConfigDetailedResponse> createBREConfig(
            @Valid @RequestBody CreateBREConfigRequest request) {
        BREConfigDetailedResponse response = breConfigWriteService.createBREConfig(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @RequirePermission(permissionName = "READ_BRE_CONFIG")
    public ResponseEntity<PaginatedResponse<BREConfigResponse>> getAllBREConfigs(
            @Valid @ModelAttribute PaginationRequest paginationRequest) {
        PaginatedResponse<BREConfigResponse> response =
                breConfigReadService.getAllBREConfigs(paginationRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uname}")
    @RequirePermission(permissionName = "READ_BRE_CONFIG")
    public ResponseEntity<BREConfigDetailedResponse> getBREConfigByUname(
            @PathVariable String uname) {
        BREConfigDetailedResponse response =
                breConfigReadService.getBREConfigByUname(uname);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{uname}/gorules/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission(permissionName = "CREATE_BRE_CONFIG")
    public ResponseEntity<BREConfigRuleFileUploadResponse> uploadRuleFile(
            @PathVariable String uname,
            @RequestPart("file") MultipartFile file) {
        BREConfigRuleFileUploadResponse response = breConfigWriteService.uploadRuleFile(uname, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
