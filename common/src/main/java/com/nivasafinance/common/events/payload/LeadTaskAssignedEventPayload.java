package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/*
 * Used by notification data provider for LEAD_TASK_ASSIGNED event
 */
@Value
@Builder
public class LeadTaskAssignedEventPayload {
    /** (recipient for notifications). */
    String username;
    UUID taskIdentifier;
    /** Task name/title */
    String taskName;
    /** Task config key e.g. CALL_CUSTOMER). */
    String taskConfigKey;
    UUID leadIdentifier;
}
