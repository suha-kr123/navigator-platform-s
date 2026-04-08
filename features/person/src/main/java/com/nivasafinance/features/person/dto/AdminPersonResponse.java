package com.nivasafinance.features.person.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPersonResponse {
    private String firstName;
    private String middleName;
    private String lastName;
    private String displayName;
    private String email;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Boolean deleted;
    private String primaryMobileNumber;
}
