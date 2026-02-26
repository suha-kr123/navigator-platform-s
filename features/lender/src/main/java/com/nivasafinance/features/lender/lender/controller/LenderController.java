package com.nivasafinance.features.lender.lender.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lender.lender.dto.LenderRequestData;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.dto.LenderSearchRequest;
import com.nivasafinance.features.lender.lender.dto.LenderWithOfficesResponse;
import com.nivasafinance.features.lender.lender.dto.UpdateLenderRequest;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import com.nivasafinance.features.lender.lender.service.LenderReadService;
import com.nivasafinance.features.lender.lender.service.LenderWriteService;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/lender")
public class LenderController {

    private final LenderReadService lenderReadService;
    private final LenderOfficeReadService lenderOfficeReadService;
    private final LenderWriteService lenderWriteService;

    @Autowired
    public LenderController(LenderReadService lenderReadService, LenderOfficeReadService lenderOfficeReadService,
                            LenderWriteService lenderWriteService) {
        this.lenderReadService = lenderReadService;
        this.lenderOfficeReadService = lenderOfficeReadService;
        this.lenderWriteService = lenderWriteService;
    }

    @PostMapping
    @RequireRole(value = {"ADMIN"})
    public ResponseEntity<LenderResponseData> createLender(@Valid @RequestBody LenderRequestData lenderRequestData) {
        LenderResponseData response = lenderWriteService.create(lenderRequestData);
        return ResponseEntity.status(201).body(response);
    }

    @PatchMapping("/{id}")
    @RequireRole(value = {"ADMIN"})
    public ResponseEntity<LenderResponseData> updateLender(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLenderRequest request) {
        LenderResponseData response = lenderWriteService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/activate-deactivate")
    @RequireRole(value = {"ADMIN"})
    public ResponseEntity<LenderResponseData> activateDeactivateLender(@PathVariable UUID id) {
        LenderResponseData response = lenderWriteService.activateDeactivateLender(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    @RequireRole(value = {"ADMIN"})
    public ResponseEntity<PaginatedResponse<LenderResponseData>> searchLenders(
            @Valid PaginationRequest paginationRequest,
            @Valid @RequestBody LenderSearchRequest searchRequest) {
        return ResponseEntity.ok(lenderReadService.searchLenders(paginationRequest, searchRequest));
    }

    @GetMapping("/paginated")
    @RequireRole(value = {"ADMIN"})
    public ResponseEntity<PaginatedResponse<LenderResponseData>> getLendersPaginated(
            @Valid PaginationRequest paginationRequest,
            @RequestParam(required = false) LenderStatus status) {
        return ResponseEntity.ok(lenderReadService.getLendersPaginated(paginationRequest, status));
    }

    @GetMapping
    @RequirePermission(permissionName = "READ_LENDER")
    public List<LenderWithOfficesResponse> getAllLendersWithOffices() {
        return lenderReadService.getAllByStatus(LenderStatus.ACTIVE).stream()
                .map(lender -> {
                    List<com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData> offices =
                            lenderOfficeReadService.getByLenderKeyAndStatus(lender.getKey(), LenderOfficeStatus.ACTIVE);
                    return new LenderWithOfficesResponse(
                            lender.getName(),
                            lender.getKey(),
                            offices
                    );
                })
                .collect(Collectors.toList());
    }
}

