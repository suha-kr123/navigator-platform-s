package com.nivasafinance.features.usermanagement.dto;

import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank
    private String username;

    private UserStatus status;

    @NotNull
    @Valid
    private PersonCreateRequest person;
}


