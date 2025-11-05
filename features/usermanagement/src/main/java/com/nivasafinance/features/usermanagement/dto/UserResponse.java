package com.nivasafinance.features.usermanagement.dto;

import com.nivasafinance.features.usermanagement.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private Long personId;
    private String username;
    private UserStatus status;
}

