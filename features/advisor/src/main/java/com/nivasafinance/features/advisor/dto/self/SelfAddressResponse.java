package com.nivasafinance.features.advisor.dto.self;

import com.nivasafinance.common.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfAddressResponse {
    private String id;
    private AddressType addressType;
    private String address;
    private String pincode;
    private String district;
    private String taluka;
    private String districtCode;
    private String talukaCode;
    private String villageCode;
    private String villageName;
    private Boolean isServiceable;
}
