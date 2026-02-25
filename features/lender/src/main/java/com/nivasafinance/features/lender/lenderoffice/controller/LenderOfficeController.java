package com.nivasafinance.features.lender.lenderoffice.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeRequestData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeSearchRequest;
import com.nivasafinance.features.lender.lenderoffice.dto.UpdateLenderOfficeRequest;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeWriteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lender/{lenderKey}/office")
public class LenderOfficeController {

    private final LenderOfficeReadService lenderOfficeReadService;
    private final LenderOfficeWriteService lenderOfficeWriteService;

    @Autowired
    public LenderOfficeController(LenderOfficeReadService lenderOfficeReadService,
                                  LenderOfficeWriteService lenderOfficeWriteService) {
        this.lenderOfficeReadService = lenderOfficeReadService;
        this.lenderOfficeWriteService = lenderOfficeWriteService;
    }

    @PostMapping
    @RequireRole(value = {"ADMIN"})
    public ResponseEntity<LenderOfficeReponseData> createLenderOffice(
            @PathVariable String lenderKey,
            @Valid @RequestBody LenderOfficeRequestData lenderOfficeRequestData) {
        LenderOfficeReponseData response = lenderOfficeWriteService.create(lenderKey, lenderOfficeRequestData);
        return ResponseEntity.status(201).body(response);
    }

    @PatchMapping("/{officeId}")
    @RequireRole(value = {"ADMIN"})
    public ResponseEntity<LenderOfficeReponseData> updateLenderOffice(
            @PathVariable String lenderKey,
            @PathVariable UUID officeId,
            @Valid @RequestBody UpdateLenderOfficeRequest request) {
        LenderOfficeReponseData response = lenderOfficeWriteService.updateOffice(officeId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{officeId}/activate-deactivate")
    @RequireRole(value = {"ADMIN"})
    public ResponseEntity<LenderOfficeReponseData> activateDeactivateLenderOffice(
            @PathVariable String lenderKey,
            @PathVariable UUID officeId) {
        LenderOfficeReponseData response = lenderOfficeWriteService.activateDeactivateLenderOffice(officeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    @RequireRole(value = {"ADMIN"})
    public ResponseEntity<PaginatedResponse<LenderOfficeReponseData>> searchLenderOffices(
            @PathVariable String lenderKey,
            @Valid PaginationRequest paginationRequest,
            @Valid @RequestBody LenderOfficeSearchRequest searchRequest) {
        return ResponseEntity.ok(lenderOfficeReadService.searchLenderOffices(lenderKey, paginationRequest, searchRequest));
    }

    @GetMapping("/paginated")
    @RequireRole(value = {"ADMIN"})
    public ResponseEntity<PaginatedResponse<LenderOfficeReponseData>> getLenderOfficesPaginated(
            @PathVariable String lenderKey,
            @Valid PaginationRequest paginationRequest,
            @RequestParam(required = false) LenderOfficeStatus status) {
        return ResponseEntity.ok(lenderOfficeReadService.getLenderOfficesPaginated(lenderKey, paginationRequest, status));
    }

    @GetMapping
    @RequirePermission(permissionName = "READ_LENDER")
    public List<LenderOfficeReponseData> getOfficesByLender(
            @PathVariable String lenderKey,
            @RequestParam(defaultValue = "ACTIVE") LenderOfficeStatus status) {
        return lenderOfficeReadService.getByLenderKeyAndStatus(lenderKey, status);
    }
}

