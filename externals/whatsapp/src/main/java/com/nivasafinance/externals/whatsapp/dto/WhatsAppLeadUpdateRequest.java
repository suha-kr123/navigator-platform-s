package com.nivasafinance.externals.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppLeadUpdateRequest {

    private UUID leadIdentifier;

    private String mobileNumber;

    private String statusAction;

    private String reasonCode;

    private LocalDate holdFollowUpDate;

    private Integer holdFollowUpInDays;

    private String onHoldReasonCode;

    private String alternativeMobileNumber;

    private Boolean alternativeMobileIsWhatsappAvailable;
}
