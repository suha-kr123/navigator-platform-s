package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAdvisorBasicResponse {
    private UUID advisorIdentifier;
    private String name;
    private String mobileNumber;
    private AdvisorStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String officeKey;
    private String username;
    private Boolean deleted;
}
