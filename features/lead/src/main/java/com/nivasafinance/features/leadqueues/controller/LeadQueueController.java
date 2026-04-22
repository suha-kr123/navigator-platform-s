package com.nivasafinance.features.leadqueues.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.leadqueues.dto.LeadQueueResponse;
import com.nivasafinance.features.leadqueues.dto.QueueConfigResponse;
import com.nivasafinance.features.leadqueues.dto.QueueWorkbenchResponse;
import com.nivasafinance.features.leadqueues.service.QueueService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/lead-queues")
@AllArgsConstructor
public class LeadQueueController {

    private final QueueService queueService;

    @GetMapping
    @RequirePermission(permissionName = "READ_LEAD")
    public ResponseEntity<List<QueueConfigResponse>> getQueuesForCurrentUser() {
        return ResponseEntity.ok(queueService.fetchQueueForAgent(UserContext.getUsername()));
    }

    @GetMapping("/{queueName}/active-claim")
    @RequirePermission(permissionName = "READ_LEAD")
    public ResponseEntity<LeadQueueResponse> getMyActiveClaimInQueue(@PathVariable String queueName) {
        return queueService
                .getMyActiveClaim(queueName, UserContext.getUsername())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Current (non-expired) claim and the next listable lead for this user — for agent UIs. Full
     * queue is still {@code GET /{queueName}/leads} (paginated).
     */
    @GetMapping("/{queueName}/workbench")
    @RequirePermission(permissionName = "READ_LEAD")
    public ResponseEntity<QueueWorkbenchResponse> getQueueWorkbench(@PathVariable String queueName) {
        return ResponseEntity.ok(
                queueService.getQueueWorkbench(queueName, UserContext.getUsername()));
    }

    @GetMapping("/{queueName}/leads")
    @RequirePermission(permissionName = "READ_LEAD")
    public ResponseEntity<PaginatedResponse<LeadQueueResponse>> getLeadsInQueue(
            @PathVariable String queueName,
            @Valid PaginationRequest paginationRequest) {
        return ResponseEntity.ok(queueService.getLeadsForQueue(queueName, paginationRequest));
    }

    @GetMapping("/entries/by-lead/{leadIdentifier}")
    @RequirePermission(permissionName = "READ_LEAD")
    public ResponseEntity<LeadQueueResponse> getEntryForLead(@PathVariable UUID leadIdentifier) {
        return ResponseEntity.ok(queueService.getLeadQueueResponse(leadIdentifier));
    }

    @PostMapping("/{queueName}/leads/{leadIdentifier}/claim")
    @RequirePermission(permissionName = "UPDATE_LEAD")
    public ResponseEntity<LeadQueueResponse> claim(
            @PathVariable String queueName,
            @PathVariable UUID leadIdentifier) {
        return ResponseEntity.ok(
                queueService.claimLead(leadIdentifier, queueName, UserContext.getUsername()));
    }

    @PostMapping("/{queueName}/leads/{leadIdentifier}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(permissionName = "UPDATE_LEAD")
    public void release(
            @PathVariable String queueName,
            @PathVariable UUID leadIdentifier) {
        queueService.releaseLead(leadIdentifier, queueName, UserContext.getUsername());
    }

    @PostMapping("/{queueName}/leads/{leadIdentifier}/heartbeat")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(permissionName = "UPDATE_LEAD")
    public void heartbeat(
            @PathVariable String queueName,
            @PathVariable UUID leadIdentifier) {
        queueService.heartbeat(leadIdentifier, queueName, UserContext.getUsername());
    }
}
