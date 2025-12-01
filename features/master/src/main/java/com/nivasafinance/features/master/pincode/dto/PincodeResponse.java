package com.nivasafinance.features.master.pincode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PincodeResponse {
    private String pincode;
    private String district;
    private String state;
    private String country;
    private String taluka;
    private String districtCode;
    private String stateCode;
    private String countryCode;
    private String talukaCode;
    private Long districtId;
    private Long stateId;
    private Long countryId;
    private Long talukaId;
    private Boolean isServicable;
}

