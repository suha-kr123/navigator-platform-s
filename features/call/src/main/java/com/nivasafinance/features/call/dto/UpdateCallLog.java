package com.nivasafinance.features.call.dto;

import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCallLog {
    private CallStatus status;
    private CallLog.RecordingDetails recordingDetails;
    private CallLog.CompletionDetails completionDetails;
}
