package com.nivasafinance.features.master.location.dto;

import com.nivasafinance.common.base.model.MasterLanguageData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDisplayNames {
    private String countryName;
    private String stateName;
    private MasterLanguageData regionValue;
    private MasterLanguageData districtValue;
    private MasterLanguageData talukaValue;
    private MasterLanguageData villageValue;
    private MasterLanguageData operatingAreaValue;
}
