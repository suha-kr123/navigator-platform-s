package com.nivasafinance.features.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCompletionRule {
    private String taskConfigKey;
    private String breRuleUname;
}
