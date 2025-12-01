package com.nivasafinance.common.dto;

import com.nivasafinance.common.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddressRequest {
    private AddressType addressType;
    private String address;

    private PincodeRequest pincode;
    private AddressLocationRequest location;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PincodeRequest {
        private String pincode;
        private String villageCode;
        private String villageName;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressLocationRequest {
        private String districtCode;
        private String stateCode;
        private String countryCode;
        private String talukaCode;
        private String villageCode;
        private String villageName;
    }
}
