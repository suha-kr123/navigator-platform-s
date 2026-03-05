package com.nivasafinance.features.offices.controller;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.offices.dto.OfficeActivateDeactivateRequest;
import com.nivasafinance.features.offices.dto.OfficeCreateRequest;
import com.nivasafinance.features.offices.dto.OfficeMoveRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.dto.OfficeTreeNodeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.offices.service.OfficeWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/admin/offices")
@RequiredArgsConstructor
public class AdminOfficeController {

    private final OfficeWriteService officeWriteService;
    private final OfficeReadService officeReadService;

    @PostMapping("/{key}/move")
    @RequireRole({"ADMIN"})
    public ResponseEntity<OfficeResponse> moveOffice(@PathVariable String key, @Valid @RequestBody OfficeMoveRequest request) {
        OfficeResponse response = officeWriteService.moveOffice(key, request.getNewParentKey());
        return ResponseEntity.ok(response);
    }

    /**
     * One level at a time: use parentKey for direct children (omit for roots).
     * When search is provided, returns matching offices with full path; parentKey is ignored.
     */
    @GetMapping("/tree")
    @RequireRole({"ADMIN"})
    public ResponseEntity<List<OfficeTreeNodeResponse>> getOfficeTree(
            @RequestParam(value = "parentKey", required = false) String parentKey,
            @RequestParam(value = "search", required = false) String search) {
        List<OfficeTreeNodeResponse> tree = officeReadService.getOfficeTree(parentKey, search);
        return ResponseEntity.ok(tree);
    }

    @PostMapping
    @RequireRole({"ADMIN"})
    public ResponseEntity<OfficeResponse> createOffice(@Valid @RequestBody OfficeCreateRequest request) {
        OfficeResponse createdOffice = officeWriteService.createOffice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOffice);
    }


    @PostMapping("/{key}/activate-deactivate")
    @RequireRole({"ADMIN"})
    public ResponseEntity<OfficeResponse> activateDeactivateOffice(
            @PathVariable String key,
            @Valid @RequestBody OfficeActivateDeactivateRequest request) {
        OfficeResponse response = Boolean.TRUE.equals(request.getActive())
                ? officeWriteService.activateOffice(key)
                : officeWriteService.deactivateOfficeCascade(key);
        return ResponseEntity.ok(response);
    }
}
