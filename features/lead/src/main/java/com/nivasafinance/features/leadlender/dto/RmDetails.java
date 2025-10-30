package com.nivasafinance.features.leadlender.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RmDetails {
    
    private String name;
    
    @Size(min = 10, max = 10, message = "Mobile number must be exactly 10 digits")
    private String mobileNumber;
}
