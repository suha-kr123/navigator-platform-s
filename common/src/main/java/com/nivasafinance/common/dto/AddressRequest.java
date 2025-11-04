package com.nivasafinance.common.dto;

import com.nivasafinance.common.enums.AddressType;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddressRequest {
    private AddressType addressType;
    private String addressLineOne;
    private String addressLineTwo;
    @NonNull
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits")
    private String pincode;
    private String area;
}
