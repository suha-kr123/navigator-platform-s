package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.dto.GeoData;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePropertyDetailsRequest {
    
    @Valid
    private AddressRequest address;
    
    private GeoData geoData;
}

