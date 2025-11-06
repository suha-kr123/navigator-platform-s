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
public class TaskConfigDetails {
    private List<String> allowedRoles;
    private List<CodeValueResponse> outcomes;
    private Boolean rescheduledAllowed;
    private List<CodeValueResponse> rescheduleReasons;
}

