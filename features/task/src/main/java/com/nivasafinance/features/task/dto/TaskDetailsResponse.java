package com.nivasafinance.features.task.dto;

import com.nivasafinance.common.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDetailsResponse {
    private UUID entityId;
    private EntityType entityType;
    private PreferredCallWindow preferredCallWindow;
    private String creatorRemarks;
    private Integer iterationCount;
    private UUID rescheduledFromTaskIdentifier;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PreferredCallWindow {
        private LocalDateTime start;
        private LocalDateTime end;
    }
}

