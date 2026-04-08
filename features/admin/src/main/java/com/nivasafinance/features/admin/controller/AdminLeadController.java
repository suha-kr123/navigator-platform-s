package com.nivasafinance.features.admin.controller;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.admin.service.AdminLeadService;
import com.nivasafinance.features.lead.dto.AdminLeadSearchResponse;
import com.nivasafinance.features.lead.dto.LeadSearchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/admin/leads")
@RequiredArgsConstructor
public class AdminLeadController {

    private final AdminLeadService adminLeadService;

    @PostMapping("/{leadId}/delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> deleteLead(@PathVariable UUID leadId) {
        adminLeadService.deleteLead(leadId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{leadId}/undo-delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> undoDeleteLead(@PathVariable UUID leadId) {
        adminLeadService.undoDeleteLead(leadId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<AdminLeadSearchResponse>> adminSearchLeads(
            @Valid PaginationRequest paginationRequest,
            @Valid @RequestBody LeadSearchRequest searchRequest
    ) {
        return ResponseEntity.ok(adminLeadService.adminSearchLeads(paginationRequest, searchRequest));
    }

    @GetMapping("/deleted")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<AdminLeadSearchResponse>> getDeletedLeads(
            @Valid PaginationRequest paginationRequest
    ) {
        return ResponseEntity.ok(adminLeadService.getDeletedLeads(paginationRequest));
    }
}
