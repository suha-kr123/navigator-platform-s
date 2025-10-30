package com.nivasafinance.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TenureType {
    
    MONTH("Month"),
    YEAR("Year"),
    DAY("Day");
    
    private final String displayName;
}

