package com.nivasafinance.services.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCreateUserRequest {
    private String phone;
    private String password;
    private String email;
    private Map<String, Object> userMetadata;
}
