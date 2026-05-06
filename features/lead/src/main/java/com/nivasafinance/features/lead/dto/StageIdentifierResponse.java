package com.nivasafinance.features.lead.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageIdentifierResponse {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String identifier;
}
