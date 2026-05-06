package com.nivasafinance.features.otp.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpScope {
    private OtpSubject primary;

    @Builder.Default
    private List<OtpSubject> relatedSubjects = new ArrayList<>();
}
