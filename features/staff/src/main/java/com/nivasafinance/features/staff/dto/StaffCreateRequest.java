package com.nivasafinance.features.staff.dto;

import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffCreateRequest {

    @NotBlank
    private String username;

    private UserStatus status;

    @NotBlank
    private String officeKey;

    @NotNull
    @Valid
    private PersonCreateRequest person;
}

