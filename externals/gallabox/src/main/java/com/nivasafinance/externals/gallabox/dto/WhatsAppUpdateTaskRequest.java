package com.nivasafinance.externals.gallabox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppUpdateTaskRequest {

    private UUID leadIdentifier;

    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobileNumber;

    @NotBlank(message = "Task config key is required")
    private String taskConfigKey;

    private String preferredStartTime;

    private String preferredEndTime;

    private String creatorRemarks;
}
