package com.nivasafinance.features.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTaskAction {

    public static final String TYPE_CREATE_TASK = "CREATE_TASK";
    public static final String TYPE_MOVE_STAGE = "MOVE_STAGE";
    public static final String TYPE_CLOSE_TASKS = "CLOSE_TASKS";
    public static final String TYPE_CHANGE_SUBSTAGE = "CHANGE_SUBSTAGE";

    private String type;
    private String taskConfigKey;
    private String targetStageKey;
    private String targetSubStageKey;
    private String assignTo;
    private String outcome;
    private LocalDateTime dueDate;
    private Boolean autoExecute;
}
