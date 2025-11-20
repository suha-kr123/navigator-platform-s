package com.nivasafinance.features.call.dto;

import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.enums.CallSource;
import com.nivasafinance.features.call.enums.CallStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallLogResponse {
    private Long id;
    private UUID identifier;
    private CallProvider provider;
    private String providerId;
    private String callerId;
    private String fromNumber;
    private String toNumber;
    private CallDirection direction;
    private CallSource source;
    private CallStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private CallLog.RecordingDetails recordingDetails;
    private CallLog.CompletionDetails completionDetails;

    public static CallLogResponse toCallLogResponse(CallLog callLog) {
        return CallLogResponse.builder()
                .id(callLog.getId())
                .identifier(callLog.getIdentifier())
                .provider(callLog.getProvider())
                .providerId(callLog.getProviderId())
                .callerId(callLog.getCallerId())
                .fromNumber(callLog.getFromNumber())
                .toNumber(callLog.getToNumber())
                .direction(callLog.getDirection())
                .source(callLog.getSource())
                .status(callLog.getStatus())
                .createdBy(callLog.getCreatedBy())
                .createdAt(callLog.getCreatedAt())
                .recordingDetails(callLog.getRecordingDetails())
                .completionDetails(callLog.getCompletionDetails())
                .build();
    }
}
