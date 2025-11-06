package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.dto.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "Address is required")
    private AddressRequest address;
}

