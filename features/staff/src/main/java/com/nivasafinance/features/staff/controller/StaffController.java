package com.nivasafinance.features.staff.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.service.StaffReadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffReadService staffReadService;

    @GetMapping
    @RequirePermission(permissionName = "READ_STAFF")
    public ResponseEntity<PaginatedResponse<StaffResponse>> getStaff(
            @RequestParam(value = "officeKey", required = false) String officeKey,
            @RequestParam(value = "name", required = false) String name,
            @Valid PaginationRequest paginationRequest
    ) {
        PaginatedResponse<StaffResponse> staff = staffReadService.getStaff(officeKey, name, paginationRequest);
        return ResponseEntity.ok(staff);
    }
    @GetMapping("/me")
    @RequirePermission(permissionName = "READ_STAFF")
    public ResponseEntity<StaffResponse> getCurrentStaff() {
        StaffResponse staff = staffReadService.getCurrentStaff();
        return ResponseEntity.ok(staff);
    }


}
