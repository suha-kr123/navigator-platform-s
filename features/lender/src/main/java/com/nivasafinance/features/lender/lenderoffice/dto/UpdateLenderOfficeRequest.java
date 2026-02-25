package com.nivasafinance.features.lender.lenderoffice.dto;

import com.nivasafinance.common.dto.AddressRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLenderOfficeRequest {
    private String name;
    private AddressRequest createAddressRequest;
}
