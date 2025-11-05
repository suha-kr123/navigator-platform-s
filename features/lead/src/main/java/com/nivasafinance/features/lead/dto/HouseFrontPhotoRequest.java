package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.dto.GeoData;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseFrontPhotoRequest {
    
    @NotNull(message = "GeoData is required")
    private GeoData geoData;
}

