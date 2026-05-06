package com.nivasafinance.features.otp.core.dto;

import com.nivasafinance.features.otp.core.enums.OtpStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpTrackedToken {
    private Long trackingId;
    private Long tokenId;
    private String otp;
    private String relatesTo;
    private LocalDateTime createdAt;
    private OtpStatus status;
}
