package com.nivasafinance.features.referral.dto;

import java.util.UUID;

import com.nivasafinance.features.referral.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralCodeRegistryResponse {

    private String referralCode;
    private EntityType entityType;
    private UUID entityIdentifier;

}