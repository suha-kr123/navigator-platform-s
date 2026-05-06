package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.lead.dto.LeadDashboardFilters;
import com.nivasafinance.features.lead.dto.LeadWhatsappLogResponse;
import com.nivasafinance.features.lead.service.LeadWhatsappLogReadService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadIdentifier}/whatsapp")
@AllArgsConstructor
public class LeadWhatsappLogController {

    private final LeadWhatsappLogReadService leadWhatsappLogReadService;

    @GetMapping("/logs")
    @RequirePermission(permissionName = "READ_LEAD")
    public ResponseEntity<PaginatedResponse<LeadWhatsappLogResponse>> getWhatsappMessages(
            @PathVariable UUID leadIdentifier,
            @ModelAttribute LeadDashboardFilters filters,
            @Valid PaginationRequest paginationRequest
    ) {
        PaginatedResponse<LeadWhatsappLogResponse> response =
                leadWhatsappLogReadService.getWhatsappMessages(leadIdentifier, filters, paginationRequest);
        return ResponseEntity.ok(response);
    }
}
