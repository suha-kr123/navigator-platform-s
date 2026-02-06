package com.nivasafinance.features.person.dto;

import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonResponse {
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String displayName;
    private List<MobileNumberDetails> mobileNumbers;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Map<String, Object> extData;
    private List<Long> cbEnquiryId;
    private Person.CreditBureauDetails cbDetails;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}

