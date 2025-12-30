package com.nivasafinance.features.leadstages.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.features.leadstages.dto.BulkChangeAssignmentRequest;
import com.nivasafinance.features.leadstages.dto.BulkChangeAssignmentResponse;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/stages")
@AllArgsConstructor
public class LeadStageBulkController {

    private final LeadStageHistoryWriteService leadStageHistoryWriteService;

    @PostMapping("/bulk-assignment")
    @RequirePermission(permissionName = "UPDATE_LEAD_STAGES")
    public ResponseEntity<BulkChangeAssignmentResponse> bulkChangeAssignment(
            @Valid @RequestBody BulkChangeAssignmentRequest request) {
        return ResponseEntity.ok(leadStageHistoryWriteService.bulkChangeAssignment(request));
    }
}

