package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.PatchAddressData;
import com.nivasafinance.common.dto.GeoData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchPropertyDetailsRequest {

    private Optional<PatchAddressData> address;
    private Optional<GeoData> geoData;
    private Optional<String> propertyType;
    private Optional<String> propertyConstructionStage;
    private Optional<String> owner;
    private Optional<String> ownerRelation;
    private Optional<PropertyMeasurementDetailsData> propertyMeasurementDetails;
    private Optional<PatchDocumentChecklistRequest> documentChecklist;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyMeasurementDetailsData {
        private String buildUpArea;
        private String siteArea;
    }
}
