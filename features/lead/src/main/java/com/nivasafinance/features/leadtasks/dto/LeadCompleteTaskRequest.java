package com.nivasafinance.features.leadtasks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.task.dto.CompleteTaskRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadCompleteTaskRequest {

    @NotNull(message = "Task identifier is required")
    private UUID taskIdentifier;

    @NotBlank(message = "Outcome is required")
    private String outcomeCodeValueKey;

    private String remarks;

    private Map<String, Object> locationDetails;

    public static CompleteTaskRequest toCompleteTaskRequest(LeadCompleteTaskRequest request) {

        CompleteTaskRequest.OutcomeDetailsRequest outcomeDetails = null;

        if (request.getRemarks() != null || ValidationUtils.isNonNull(request.getLocationDetails())) {
            outcomeDetails = CompleteTaskRequest.OutcomeDetailsRequest.builder()
                    .remarks(request.getRemarks())
                    .locationDetails(
                            ValidationUtils.isNonNull(request.getLocationDetails())
                                    ? request.getLocationDetails()
                                    : null)
                    .build();
        }

        return CompleteTaskRequest.builder()
                .taskIdentifier(request.getTaskIdentifier())
                .outcomeCodeValueKey(request.getOutcomeCodeValueKey())
                .outcomeDetails(outcomeDetails)
                .build();
    }

}
