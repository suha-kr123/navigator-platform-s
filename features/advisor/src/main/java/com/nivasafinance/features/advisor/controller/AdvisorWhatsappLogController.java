package com.nivasafinance.features.advisor.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardFilters;
import com.nivasafinance.features.advisor.dto.AdvisorWhatsappLogResponse;
import com.nivasafinance.features.advisor.service.AdvisorWhatsappLogReadService;
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
@RequestMapping(ApiConstants.V1 + "/advisors/{advisorIdentifier}/whatsapp")
@AllArgsConstructor
public class AdvisorWhatsappLogController {

    private final AdvisorWhatsappLogReadService advisorWhatsappLogReadService;

    @GetMapping("/logs")
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<PaginatedResponse<AdvisorWhatsappLogResponse>> getWhatsappMessages(
            @PathVariable UUID advisorIdentifier,
            @ModelAttribute AdvisorDashboardFilters filters,
            @Valid PaginationRequest paginationRequest
    ) {
        PaginatedResponse<AdvisorWhatsappLogResponse> response =
                advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, filters, paginationRequest);
        return ResponseEntity.ok(response);
    }
}
