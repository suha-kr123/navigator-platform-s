package com.nivasafinance.features.bre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BREConfigRuleFileUploadResponse {
    private Long ruleJsonFileId;
    private UUID documentIdentifier;
}
