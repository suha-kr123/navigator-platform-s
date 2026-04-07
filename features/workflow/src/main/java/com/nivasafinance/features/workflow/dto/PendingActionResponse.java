package com.nivasafinance.features.workflow.dto;

import com.nivasafinance.common.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingActionResponse {

    private UUID actionIdentifier;
    private String actionType;
    private UUID entityIdentifier;
    private EntityType entityType;
    private String currentStageKey;

    private UUID sourceTaskIdentifier;
    private String sourceTaskConfigKey;
    private String sourceOutcome;

    private String taskConfigKey;
    private String taskName;
    private String targetStageKey;
    private String targetStageName;
    private String targetSubStageKey;

    private FieldRequirements fields;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldRequirements {
        private FieldMeta assignTo;
        private FieldMeta dueDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldMeta {
        private boolean required;
        private List<String> allowedRoles;
    }
}
