package com.nivasafinance.features.staff.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.staff.dto.StaffCreateRequest;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.service.StaffReadService;
import com.nivasafinance.features.staff.service.StaffWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffReadService staffReadService;
    private final StaffWriteService staffWriteService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<StaffResponse>> getStaff(
            @RequestParam(value = "officeKey", required = false) String officeKey,
            @RequestParam(value = "name", required = false) String name,
            @Valid PaginationRequest paginationRequest
    ) {
        PaginatedResponse<StaffResponse> staff = staffReadService.getStaff(officeKey, name, paginationRequest);
        return ResponseEntity.ok(staff);
    }

    @PostMapping
    public ResponseEntity<StaffResponse> createStaff(@Valid @RequestBody StaffCreateRequest request) {
        StaffResponse response = staffWriteService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<StaffResponse> getCurrentStaff() {
        StaffResponse staff = staffReadService.getCurrentStaff();
        return ResponseEntity.ok(staff);
    }
}

