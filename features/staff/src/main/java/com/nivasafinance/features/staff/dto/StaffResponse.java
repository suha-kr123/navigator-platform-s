package com.nivasafinance.features.staff.dto;

import com.nivasafinance.features.usermanagement.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {

    private Long id;

    private UUID identifier;

    private UserResponse userResponse;

    private String officeKey;
    private String officeName;
    private String referralCode;
}
