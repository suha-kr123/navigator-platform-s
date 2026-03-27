package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.leadbre.enums.LeadBREResultStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadBREResultResponse {

    private UUID identifier;
    private LeadBREResultStatus status;
    private String input;
    private String output;
}
