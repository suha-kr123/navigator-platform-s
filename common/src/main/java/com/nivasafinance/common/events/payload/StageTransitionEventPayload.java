package com.nivasafinance.common.events.payload;

import com.nivasafinance.common.enums.EntityType;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class StageTransitionEventPayload {

    EntityType entityType;
    UUID entityIdentifier;
    Long entityId;
    String fromStageKey;
    String toStageKey;
    String assignedTo;
    String remarks;
}

