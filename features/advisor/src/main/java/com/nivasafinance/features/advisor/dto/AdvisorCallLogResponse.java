package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.call.dto.CallLogResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdvisorCallLogResponse {
    private CallLogResponse callLogDetails;
}
