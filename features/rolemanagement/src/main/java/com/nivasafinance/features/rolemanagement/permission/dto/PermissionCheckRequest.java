package com.nivasafinance.features.rolemanagement.permission.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCheckRequest {
    @NotEmpty(message = "Permission names list cannot be empty")
    private List<String> permissionNames;
}
