package com.nivasafinance.features.lender.lenderoffice.dto;

import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LenderOfficeRequestData {
    private String name;
    private String key;
    private String lenderKey;
    private AddressRequest createAddressRequest;
    private LenderOfficeStatus status;
}

