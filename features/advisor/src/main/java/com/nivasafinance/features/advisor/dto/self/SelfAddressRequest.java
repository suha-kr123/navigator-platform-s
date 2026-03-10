package com.nivasafinance.features.advisor.dto.self;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfAddressRequest {
    private Optional<String> address;
    private Optional<SelfPincodeRequest> pincode;
    private Optional<SelfAddressLocationRequest> location;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelfPincodeRequest {
        private Optional<String> pincode;
        private Optional<String> villageCode;
        private Optional<String> villageName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelfAddressLocationRequest {
        private Optional<String> districtCode;
        private Optional<String> stateCode;
        private Optional<String> countryCode;
        private Optional<String> talukaCode;
        private Optional<String> villageCode;
        private Optional<String> villageName;
    }
}
