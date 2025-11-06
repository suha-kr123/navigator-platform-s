package com.nivasafinance.features.lender.lender.dto;

import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LenderRequestData {
    private String key;
    private String name;
    private LenderStatus status;
}

