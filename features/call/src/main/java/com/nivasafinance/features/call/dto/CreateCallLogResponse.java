package com.nivasafinance.features.call.dto;

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
public class CreateCallLogResponse {
    private Long id;
    private UUID identifier;
    private CallStatus status;
}

