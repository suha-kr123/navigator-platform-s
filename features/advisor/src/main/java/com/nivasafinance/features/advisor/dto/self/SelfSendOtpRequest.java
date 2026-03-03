package com.nivasafinance.features.advisor.dto.self;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelfSendOtpRequest {
    private String mobileNo;
}
