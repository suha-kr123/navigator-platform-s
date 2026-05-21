package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.GeoData;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDetailsResponse {
    private AddressData address;
    private GeoData geoData;
    private CodeValueResponse propertyType;
    private CodeValueResponse propertyConstructionStage;
    private String owner;
    private String ownerRelation;
    private PropertyMeasurementDetailsData propertyMeasurementDetails;
    private DocumentChecklistResponse documentChecklistResponse;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyMeasurementDetailsData {
        private BigDecimal buildUpArea;
        private BigDecimal siteArea;
        private BigDecimal buildUpValue;
    }
}

