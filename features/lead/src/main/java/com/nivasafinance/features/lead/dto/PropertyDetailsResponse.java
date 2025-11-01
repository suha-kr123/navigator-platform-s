package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.GeoData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDetailsResponse {
    private AddressData address;
    private GeoData geoData;
}

