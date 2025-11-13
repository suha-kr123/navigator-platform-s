package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.call.enums.CallStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateLeadCallResponse {
    private UUID identifier;
    private CallStatus status;
}
