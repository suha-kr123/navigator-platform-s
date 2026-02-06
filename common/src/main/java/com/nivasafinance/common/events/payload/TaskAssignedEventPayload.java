package com.nivasafinance.common.events.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivasafinance.common.enums.EntityType;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class TaskAssignedEventPayload {

    String username;
    UUID taskIdentifier;
    String taskName;
    String taskConfigKey;
    EntityType entityType;
    UUID entityId;

    @JsonProperty("leadIdentifier")
    public UUID getLeadIdentifier() {
        return entityType == EntityType.LEAD ? entityId : null;
    }
}
