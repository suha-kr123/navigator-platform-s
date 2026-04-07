package com.nivasafinance.common.events.payload;

import com.nivasafinance.common.enums.EntityType;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class TaskCompletedEventPayload {
    UUID taskIdentifier;
    String taskConfigKey;
    String outcome;
    UUID entityIdentifier;
    EntityType entityType;
    String stageKey;
    String assignedTo;
}
