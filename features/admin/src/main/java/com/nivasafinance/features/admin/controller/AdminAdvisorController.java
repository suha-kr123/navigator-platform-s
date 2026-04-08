package com.nivasafinance.features.admin.controller;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.admin.service.AdminAdvisorService;
import com.nivasafinance.features.advisor.dto.AdminAdvisorBasicResponse;
import com.nivasafinance.features.advisor.dto.AdvisorSearchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/admin/advisors")
@RequiredArgsConstructor
public class AdminAdvisorController {

    private final AdminAdvisorService adminAdvisorService;

    @PostMapping("/{identifier}/delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> deleteAdvisor(@PathVariable UUID identifier) {
        adminAdvisorService.deleteAdvisor(identifier);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/undo-delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> undoDeleteAdvisor(@PathVariable UUID identifier) {
        adminAdvisorService.undoDeleteAdvisor(identifier);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<AdminAdvisorBasicResponse>> adminSearchAdvisors(
            @Valid PaginationRequest paginationRequest,
            @Valid @RequestBody AdvisorSearchRequest searchRequest
    ) {
        return ResponseEntity.ok(adminAdvisorService.adminSearchAdvisors(paginationRequest, searchRequest));
    }

    @GetMapping("/deleted")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<AdminAdvisorBasicResponse>> getDeletedAdvisors(
            @Valid PaginationRequest paginationRequest
    ) {
        return ResponseEntity.ok(adminAdvisorService.getDeletedAdvisors(paginationRequest));
    }
}
