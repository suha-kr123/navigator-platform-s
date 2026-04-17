package com.nivasafinance.common.dto;

import com.nivasafinance.common.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchAddressData {

    private Optional<String> id;
    private Optional<AddressType> addressType;
    private Optional<String> address;
    private Optional<String> pincode;
    private Optional<String> district;
    private Optional<String> country;
    private Optional<String> state;
    private Optional<String> region;
    private Optional<String> taluka;
    private Optional<String> districtCode;
    private Optional<String> regionCode;
    private Optional<String> stateCode;
    private Optional<String> countryCode;
    private Optional<String> talukaCode;
    private Optional<Long> districtId;
    private Optional<Long> regionId;
    private Optional<Long> stateId;
    private Optional<Long> countryId;
    private Optional<Long> talukaId;
    private Optional<String> villageCode;
    private Optional<Long> villageId;
    private Optional<String> villageName;
    private Optional<Boolean> isServiceable;
}
