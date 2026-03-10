package com.nivasafinance.features.advisor.dto.self;

import com.nivasafinance.features.referral.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfReferredByDetails {
    private String referredByName;
    private EntityType referredByType;
}
