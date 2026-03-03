package com.nivasafinance.services.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthVerifyOtpRequest {
    private String phone;
    private String token;
}
