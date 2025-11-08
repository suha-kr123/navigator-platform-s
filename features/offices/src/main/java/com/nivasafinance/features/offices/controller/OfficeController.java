package com.nivasafinance.features.offices.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.offices.dto.OfficeCreateRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.offices.service.OfficeWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/offices")
@AllArgsConstructor
public class OfficeController {

    private final OfficeReadService officeReadService;
    private final OfficeWriteService officeWriteService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<OfficeResponse>> getOffices(
            @RequestParam(value = "parentKey", required = false) String parentKey,
            @RequestParam(value = "name", required = false) String name,
            @Valid PaginationRequest paginationRequest
    ) {
        PaginatedResponse<OfficeResponse> offices = officeReadService.getOffices(parentKey, name, paginationRequest);
        return ResponseEntity.ok(offices);
    }

    @PostMapping
    public ResponseEntity<OfficeResponse> createOffice(@RequestBody OfficeCreateRequest request) {
        OfficeResponse createdOffice = officeWriteService.createOffice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOffice);
    }

    @GetMapping("/{key}")
    public ResponseEntity<OfficeResponse> getOffice(@PathVariable String key) {
        OfficeResponse office = officeReadService.getOfficeByKey(key);
        return ResponseEntity.ok(office);
    }
}

