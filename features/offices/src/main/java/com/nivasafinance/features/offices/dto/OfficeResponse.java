package com.nivasafinance.features.offices.dto;

import com.nivasafinance.common.dto.AddressData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeResponse {
    private Long id;
    private String name;
    private String key;
    private String code;
    private AddressData address;
    private Long parentId;
    private Boolean isActive;
}
