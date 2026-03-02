package com.nivasafinance.features.advisor.dto.self;

import com.nivasafinance.features.advisor.dto.MobileNumberDetails;
import com.nivasafinance.features.person.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfPersonalDetailsRequest {
    private Optional<String> firstName;
    private Optional<String> middleName;
    private Optional<String> lastName;
    private Optional<List<MobileNumberDetails>> mobileNumbers;
    private Optional<LocalDate> dateOfBirth;
    private Optional<Gender> gender;
}
