package com.nivasafinance.features.master.pincode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PincodeResponse {
    private String pincode;
    private List<String> area;
    private String district;
    private String state;
    private String country;
    private Boolean isServicable;
}

