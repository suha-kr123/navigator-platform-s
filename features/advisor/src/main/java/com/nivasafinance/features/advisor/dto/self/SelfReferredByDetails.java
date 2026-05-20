package com.nivasafinance.features.advisor.dto.self;

import com.nivasafinance.common.enums.ReferredByType;
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
    private ReferredByType referredByType;
}
