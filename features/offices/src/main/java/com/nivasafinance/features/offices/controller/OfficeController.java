package com.nivasafinance.features.offices.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.offices.dto.OfficeCreateRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.offices.service.OfficeWriteService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/offices")
@AllArgsConstructor
public class OfficeController {

    private final OfficeReadService officeReadService;
    private final OfficeWriteService officeWriteService;

    @PostMapping
    public ResponseEntity<OfficeResponse> createOffice(@RequestBody OfficeCreateRequest request) {
        OfficeResponse createdOffice = officeWriteService.createOffice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOffice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfficeResponse> getOffice(@PathVariable UUID id) {
        OfficeResponse office = officeReadService.getOffice(id);
        return ResponseEntity.ok(office);
    }
}

