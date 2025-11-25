package com.nivasafinance.features.workflow.dto;

import com.nivasafinance.common.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDetailsResponse {
    private Long entityId;
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

