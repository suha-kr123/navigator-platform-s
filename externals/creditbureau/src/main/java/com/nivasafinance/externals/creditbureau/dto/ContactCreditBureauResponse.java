package com.nivasafinance.externals.creditbureau.dto;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactCreditBureauResponse {
    private String firstName;
    private String lastName;
    private List<MobileNumberDetails> mobileNumberDetails;
    private List<IdentifierData> identifierData;
    private LocalDate dateOfBirth;
    private List<AddressData> address;
}
