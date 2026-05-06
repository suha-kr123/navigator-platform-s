package com.nivasafinance.externals.gallabox.dto;

import com.nivasafinance.common.dto.AddressData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppLeadResponse {
    private UUID leadIdentifier;
    private UUID contactIdentifier;
    private List<AddressData> address;
    private String preliminaryDetails;  // Changed to String to support "empty" value
    private String status;  // Changed to String to support "empty" value
    private String substatus;  // Changed to String to support "empty" value
    private String reasons;  // Changed to String to support "empty" value
    private String stage;
    private String name;
    private String productCode;
    private String productName;
}
