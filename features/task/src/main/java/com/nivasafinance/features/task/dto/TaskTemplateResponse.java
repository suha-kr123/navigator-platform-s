package com.nivasafinance.features.task.dto;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTemplateResponse {
    
    private String taskConfigKey;
    private String taskName;
    private String taskDescription;
    private List<String> allowedRoles;
    private List<CodeValueResponse> allowedOutcomes;
    private Boolean isRescheduledAllowed;
    private List<CodeValueResponse> rescheduleReasons;
}

