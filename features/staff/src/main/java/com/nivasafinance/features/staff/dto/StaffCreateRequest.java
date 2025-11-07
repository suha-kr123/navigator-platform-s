package com.nivasafinance.features.staff.dto;

import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffCreateRequest {

    @NotBlank
    private String officeKey;

    @NotNull
    @Valid
    private UserCreateRequest user;
}

