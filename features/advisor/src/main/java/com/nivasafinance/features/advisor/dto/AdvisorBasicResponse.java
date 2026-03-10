package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.features.referral.enums.EntityType;

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
public class AdvisorBasicResponse {
    private UUID advisorIdentifier;
    private String name;
    private String mobileNumber;
    private AdvisorStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String officeKey;
    private String owner;
    private String username;

    //referral details
    private String referredByCode;
    private EntityType referredByType;
    private UUID referredByIdentifier;
    private String referredByName;
    private String referredByNumber;
}

