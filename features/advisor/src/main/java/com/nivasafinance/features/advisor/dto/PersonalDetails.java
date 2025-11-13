package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
/**
 * Personal details DTO - follows lead module pattern
 * Includes person information fetched from n_person table
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalDetails {
    private String firstName;
    private String middleName;
    private String lastName;
    private List<MobileNumberDetails> mobileNumbers;
    private LocalDate dateOfBirth;
    private Gender gender;
}

