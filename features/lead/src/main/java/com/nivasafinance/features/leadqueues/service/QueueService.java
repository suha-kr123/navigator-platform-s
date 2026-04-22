package com.nivasafinance.features.leadqueues.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadqueues.dto.LeadQueueResponse;
import com.nivasafinance.features.leadqueues.dto.QueueConfigResponse;
import com.nivasafinance.features.leadqueues.dto.QueueWorkbenchResponse;

public interface QueueService {

    List<QueueConfigResponse> fetchQueueForAgent(String username);

    /**
     * Paged queue entries for the current user. Entries held by <em>another</em> user with a non-expired
     * claim are omitted. When the queue is past its reorder window ({@code last_reorder_time} + {@code reorder_time} seconds),
     * runs the configured data provider to refresh {@code n_lead_queue} before reading.
     */
    PaginatedResponse<LeadQueueResponse> getLeadsForQueue(String queueConfigName, PaginationRequest paginationRequest);

    /**
     * Agent workbench: at most the current non-expired claim and the next listable lead. Does not
     * replace paginated {@link #getLeadsForQueue} for full-queue views.
     */
    QueueWorkbenchResponse getQueueWorkbench(String queueConfigName, String username);

    /**
     * Claims the requested lead when free (or when already held by this user, extends the lock).
     * At most one non-expired claim per user per queue; to take a different lead, release first.
     * If another user holds an active lock on the requested lead, claims the first
     * available lead in the same queue by position (same as “next up” in the queue)
     * instead of failing.
     */
    LeadQueueResponse claimLead(UUID leadIdentifier, String queueConfigName, String username);

    void releaseLead(UUID leadIdentifier, String queueConfigName, String username);

    void heartbeat(UUID leadIdentifier, String queueConfigName, String username);
    
    LeadQueueResponse getLeadQueueResponse(UUID leadIdentifier);

    /**
     * The caller’s current non-expired claim in the given queue, if any.
     */
    Optional<LeadQueueResponse> getMyActiveClaim(String queueConfigName, String username);
}
