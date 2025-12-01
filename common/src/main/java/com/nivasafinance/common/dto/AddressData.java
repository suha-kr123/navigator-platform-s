package com.nivasafinance.common.dto;

import com.nivasafinance.common.enums.AddressType;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressData {
    private String id;
    private AddressType addressType;
    private String address;
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits")
    private String pincode;
    private String district;
    private String country;
    private String state;
    private String taluka;
    private String districtCode;
    private String stateCode;
    private String countryCode;
    private String talukaCode;
    private Long districtId;
    private Long stateId;
    private Long countryId;
    private Long talukaId;
    private String villageCode;
    private Long villageId;
    private String villageName;
    private Boolean isServiceable;
}
