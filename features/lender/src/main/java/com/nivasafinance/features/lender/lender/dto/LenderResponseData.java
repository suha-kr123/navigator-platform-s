package com.nivasafinance.features.lender.lender.dto;

import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LenderResponseData {
    private UUID id;
    private String name;
    private String key;
    private LenderStatus status;
}

