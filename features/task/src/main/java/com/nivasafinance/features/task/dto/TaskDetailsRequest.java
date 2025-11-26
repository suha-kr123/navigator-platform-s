package com.nivasafinance.features.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import com.nivasafinance.common.enums.EntityType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDetailsRequest {
    private UUID entityId;
    private EntityType entityType;
    private PreferredCallWindow preferredCallWindow;
    private String creatorRemarks;
    private Integer iterationCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PreferredCallWindow {
        private LocalDateTime start;
        private LocalDateTime end;
    }
}

