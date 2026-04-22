package com.nivasafinance.features.leadqueues.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * At most one active claim and one “next” listable lead for agent UIs. Full queue remains on
 * {@code GET /.../leads} with pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueWorkbenchResponse {

    /** The caller’s non-expired claim in this queue, if any. */
    private LeadQueueResponse currentClaim;

    /**
     * First listable lead in this queue (by position) that is not {@link #currentClaim}’s row, if
     * any. When the caller has no claim, this is the first listable lead overall.
     */
    private LeadQueueResponse nextAvailable;
}
