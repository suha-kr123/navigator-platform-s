package com.nivasafinance.features.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdvisorRequest {
    
    private List<MobileNumberDetails> mobileNumberDetails;
    
    private PersonalDetails personalDetails;
}

