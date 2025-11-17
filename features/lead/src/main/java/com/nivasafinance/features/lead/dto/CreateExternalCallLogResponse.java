package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.call.enums.CallStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExternalCallLogResponse {
    private UUID identifier;
    private CallStatus status;
}

