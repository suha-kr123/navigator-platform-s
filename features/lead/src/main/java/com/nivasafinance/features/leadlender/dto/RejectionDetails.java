package com.nivasafinance.features.leadlender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectionDetails {
    
    private String rejectedBy;

    private String rejectionReason;
    
    private LocalDateTime rejectionDate;
}
