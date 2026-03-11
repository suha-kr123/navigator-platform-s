package com.nivasafinance.features.person.dto;

import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.enums.Gender;
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
public class PersonCreateRequest {
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private List<MobileNumberDetails> mobileNumbers;
    private LocalDate dateOfBirth;
    private Gender gender;
}

