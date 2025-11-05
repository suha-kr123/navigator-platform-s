package com.nivasafinance.features.usermanagement.dto;

import com.nivasafinance.features.person.dto.PersonResponse;
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
    private PersonResponse personResponse;
    private String username;
    private UserStatus status;
}

