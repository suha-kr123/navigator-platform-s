package com.nivasafinance.features.workflow.dto;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.workflow.enums.TaskType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskConfig {
    private String taskConfigKey;
    private TaskType taskType;
    private List<String> allowedRoles;
    private List<CodeValueResponse> outcomes;
    private Boolean rescheduledAllowed;
    private List<CodeValueResponse> rescheduleReason;
}

