package com.nivasafinance.common.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressData {
    private String id;
    private String addressLineOne;
    private String addressLineTwo;
    @NonNull
    private String pincode;
    private String district;
    private String country;
    private String state;
    private String area;
    private Boolean isServiceable;
}
