package com.nivasafinance.features.lender.lenderoffice.dto;

import com.nivasafinance.common.dto.AddressData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LenderOfficeReponseData {
    private UUID id;
    private String name;
    private String key;
    private String lenderKey;
    private AddressData address;
}

