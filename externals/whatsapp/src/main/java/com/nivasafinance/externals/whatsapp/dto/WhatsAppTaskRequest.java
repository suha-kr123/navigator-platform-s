package com.nivasafinance.externals.whatsapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppTaskRequest {

    @NotNull(message = "Lead identifier is mandatory")
    private UUID leadIdentifier;

    @Valid
    @NotNull(message = "Task details are mandatory")
    private TaskDetails taskDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDetails {
        @NotBlank(message = "Task config key is required")
        private String taskConfigKey;

        private String assignedTo;

        private LocalDateTime dueAt;

        private String stageKey;

        private String creatorRemarks;

        private PreferredCallWindow preferredCallWindow;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PreferredCallWindow {
            private LocalDateTime start;
            private LocalDateTime end;
        }
    }
}
