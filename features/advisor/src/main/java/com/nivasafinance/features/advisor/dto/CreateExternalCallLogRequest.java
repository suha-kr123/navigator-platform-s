package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.enums.CallStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateExternalCallLogRequest {
    @NotNull(message = "Provider is required")
    private CallProvider provider;

    @NotBlank(message = "Provider ID is required")
    private String providerId;

    private String callerId;

    @NotBlank(message = "From number is required")
    private String fromNumber;

    private String toNumber;

    private CallDirection direction;

    @NotNull(message = "Status is required")
    private CallStatus status;

    private LocalDateTime createdAt;

    private CallLog.RecordingDetails recordingDetails;

    private CallLog.CompletionDetails completionDetails;
}
