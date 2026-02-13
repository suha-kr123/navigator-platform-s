package com.nivasafinance.features.advisor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdvisorRequest {

    @Valid
    @NotNull(message = "Mobile number details are mandatory")
    private MobileNumberDetails mobileNumberDetails;

    private PersonalDetails personalDetails;

    private String officeKey;

    private LocalTime preferredCallStartTime;

    private LocalTime preferredCallEndTime;

    private SourcingChannelRequest sourcingChannelRequest;

}
