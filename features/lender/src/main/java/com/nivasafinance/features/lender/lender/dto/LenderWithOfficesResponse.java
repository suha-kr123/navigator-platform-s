package com.nivasafinance.features.lender.lender.dto;

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LenderWithOfficesResponse {
    private String name;
    private String key;
    private List<LenderOfficeReponseData> offices;
}

