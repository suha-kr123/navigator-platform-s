package com.nivasafinance.features.bre.dto;

import com.nivasafinance.features.bre.enums.BREProvider;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBREConfigRequest {

    @NotNull(message = "uname is required")
    private String uname;

    @NotNull(message = "Provider is required")
    private BREProvider provider;

    private Long dataProviderId;
}
