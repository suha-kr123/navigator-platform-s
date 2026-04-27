package com.nivasafinance.features.advisor.controller.crm;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.AdvisorWhatsappNotificationResponse;
import com.nivasafinance.features.advisor.service.AdvisorWhatsappReadService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/advisors/{advisorIdentifier}/whatsapp")
@AllArgsConstructor
public class AdvisorWhatsappController {

    private final AdvisorWhatsappReadService advisorWhatsappReadService;

    @GetMapping("/notifications")
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<PaginatedResponse<AdvisorWhatsappNotificationResponse>> getWhatsappNotifications(
            @PathVariable UUID advisorIdentifier,
            @Valid PaginationRequest paginationRequest
    ) {
        PaginatedResponse<AdvisorWhatsappNotificationResponse> response =
                advisorWhatsappReadService.getWhatsappNotifications(advisorIdentifier, paginationRequest);
        return ResponseEntity.ok(response);
    }
}
