package com.nivasafinance.features.leadlender.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectLeadLenderRequest {
    
    @NotNull(message = "Reject reason is required")
    private String rejectReason;

    private String remarks;
}
