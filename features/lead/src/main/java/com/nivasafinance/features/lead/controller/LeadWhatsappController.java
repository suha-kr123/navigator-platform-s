package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.lead.dto.LeadWhatsappNotificationResponse;
import com.nivasafinance.features.lead.service.LeadWhatsappReadService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadIdentifier}/whatsapp")
@AllArgsConstructor
public class LeadWhatsappController {

    private final LeadWhatsappReadService leadWhatsappReadService;

    @GetMapping("/notifications")
    @RequirePermission(permissionName = "READ_LEAD")
    public ResponseEntity<PaginatedResponse<LeadWhatsappNotificationResponse>> getWhatsappNotifications(
            @PathVariable UUID leadIdentifier,
            @Valid PaginationRequest paginationRequest
    ) {
        PaginatedResponse<LeadWhatsappNotificationResponse> response =
                leadWhatsappReadService.getWhatsappNotifications(leadIdentifier, paginationRequest);
        return ResponseEntity.ok(response);
    }
}
