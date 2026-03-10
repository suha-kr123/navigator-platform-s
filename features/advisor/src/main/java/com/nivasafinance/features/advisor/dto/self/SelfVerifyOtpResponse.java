package com.nivasafinance.features.advisor.dto.self;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfVerifyOtpResponse {
    private String username;
    private String accessToken;
    private String refreshToken;
    private long expiresAt;
    private boolean isNewUser;
}
