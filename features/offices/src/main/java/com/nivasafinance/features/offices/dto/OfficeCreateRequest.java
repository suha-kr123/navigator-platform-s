package com.nivasafinance.features.offices.dto;

import com.nivasafinance.common.dto.AddressRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeCreateRequest {
    
    @NotNull(message = "Name is required")
    private String name;
    
    @NotNull(message = "Key is required")
    private String key;

    private AddressRequest address;
    
    private Long parentId;
}

