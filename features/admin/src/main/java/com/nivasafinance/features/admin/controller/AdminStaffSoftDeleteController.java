package com.nivasafinance.features.admin.controller;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.admin.service.AdminStaffService;
import com.nivasafinance.features.staff.dto.StaffResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/admin/staff")
@RequiredArgsConstructor
public class AdminStaffSoftDeleteController {

    private final AdminStaffService adminStaffService;

    @PostMapping("/{identifier}/delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> deleteStaff(@PathVariable UUID identifier) {
        adminStaffService.deleteStaff(identifier);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/undo-delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> undoDeleteStaff(@PathVariable UUID identifier) {
        adminStaffService.undoDeleteStaff(identifier);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<StaffResponse>> adminSearchStaff(
            @RequestParam(value = "name", required = false) String name,
            @Valid PaginationRequest paginationRequest
    ) {
        return ResponseEntity.ok(adminStaffService.adminSearchStaff(name, paginationRequest));
    }

    @GetMapping("/deleted")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<StaffResponse>> getDeletedStaff(
            @Valid PaginationRequest paginationRequest
    ) {
        return ResponseEntity.ok(adminStaffService.getDeletedStaff(paginationRequest));
    }
}
