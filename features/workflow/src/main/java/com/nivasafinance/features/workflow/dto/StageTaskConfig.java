package com.nivasafinance.features.workflow.dto;

import com.nivasafinance.features.workflow.constants.WorkflowConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageTaskConfig {
    private String fromStage;
    private List<String> taskConfigKeys;

    /**
     * Determines if this stage task config matches the given fromStage.
     * 
     * Special fromStage values:
     * - "LANDING_STAGE": Matches when there's no previous stage (landing stage entry)
     * - "DEFAULT": Matches regardless of the previous stage
     * - Any other value: Exact match with the previous stage key
     * 
     * @param actualFromStage The actual previous stage key, or null if this is a landing stage entry
     * @return true if this config should be used for the given fromStage
     */
    public boolean matches(String actualFromStage) {
        // When the stage is landing stage, from stage will be null, so we can use special value "LANDING_STAGE"
        if (WorkflowConstants.WorkflowConfigDetails.LANDING_STAGE.equals(fromStage)) {
            return WorkflowConstants.StageTask.LANDING_STAGE.equals(actualFromStage);
        }

        // When we need to create tasks regardless of the previous stage, we can use special value "DEFAULT"
        if (WorkflowConstants.WorkflowConfigDetails.DEFAULT_STAGE.equals(fromStage)) {
            return true;  // Always match regardless of actualFromStage
        }  

        // Otherwise, exact match
        return fromStage.equals(actualFromStage);
    }
}
