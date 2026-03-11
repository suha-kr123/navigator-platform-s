package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.enums.Gender;
import jakarta.validation.constraints.NotEmpty;
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
public class LeadContactPersonDetails {
    private String firstName;
    
    private String middleName;
    
    private String lastName;
    
    @NotEmpty(message = "At least one mobile number is required")
    private List<MobileNumberDetails> mobileNumbers;
    
    private LocalDate dateOfBirth;
    
    private Gender gender;

    public String email;
}

