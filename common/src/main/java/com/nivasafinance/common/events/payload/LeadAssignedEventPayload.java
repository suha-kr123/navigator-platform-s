package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class LeadAssignedEventPayload {

    String username;
    UUID leadIdentifier;
    String stageKey;
    String assignedBy;
}
