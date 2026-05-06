package com.nivasafinance.features.otp.core.dto;

import com.nivasafinance.features.otp.core.enums.OtpReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendCommand {
    private OtpReference reference;
    private String relatesTo;

    @Builder.Default
    private List<OtpRecipient> recipients = new ArrayList<>();

    private OtpScope scope;
}
