package com.nivasafinance.features.lender.lenderoffice.dto;

import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LenderOfficeSearchRequest {
    private String query;
    private LenderOfficeStatus status;
}
