package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.features.otp.core.dto.OtpRecipient;
import com.nivasafinance.features.otp.core.dto.OtpSendCommand;
import com.nivasafinance.features.otp.core.dto.OtpSubject;
import com.nivasafinance.features.otp.core.enums.OtpChannel;
import com.nivasafinance.features.otp.core.service.OtpDeliveryService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.whatsapp.WhatsAppHandler;
import com.nivasafinance.services.whatsapp.dto.TemplateParameter;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WhatsAppOtpDeliveryService implements OtpDeliveryService {

    private final ServiceFactory<WhatsAppHandler> serviceFactory;

    public WhatsAppOtpDeliveryService(ServiceFactory<WhatsAppHandler> serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @Override
    public OtpChannel getChannel() {
        return OtpChannel.WHATSAPP;
    }

    @Override
    public void send(OtpRecipient recipient, String otp, String templateName, OtpSendCommand command) {
        if (templateName == null || templateName.isBlank()) {
            throw new IllegalArgumentException("WhatsApp template name is required");
        }

        String formattedPhoneNumber = formatRecipientPhone(recipient.getDestination());
        WhatsAppHandler handler = serviceFactory.getHandler(ThirdPartyServiceList.WHATSAPP);
        WhatsAppTemplateResponse response = handler.sendTemplate(
                WhatsAppTemplateRequest.builder()
                        .phoneNumber(formattedPhoneNumber)
                        .templateName(templateName)
                        .parameters(List.of(TemplateParameter.builder()
                                .name("otp")
                                .value(otp)
                                .build()))
                        .build(),
                buildBusinessContext(command)
        );

        String status = response != null ? response.getStatus() : null;
        boolean success = status != null
                && ("sent".equalsIgnoreCase(status) || "accepted".equalsIgnoreCase(status));
        if (!success) {
            String errorMessage = response != null ? response.getErrorMessage() : null;
            String resolvedMessage = errorMessage != null && !errorMessage.isBlank()
                    ? errorMessage
                    : "Status: " + (status != null ? status : "null");
            throw new RuntimeException("WhatsApp OTP send failed: " + resolvedMessage);
        }
    }

    private String formatRecipientPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("WhatsApp recipient phone is required");
        }

        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        if (digitsOnly.length() == 10) {
            return "91" + digitsOnly;
        }
        if (digitsOnly.length() == 12 && digitsOnly.startsWith("91")) {
            return digitsOnly;
        }
        return digitsOnly;
    }

    private BusinessContext buildBusinessContext(OtpSendCommand command) {
        OtpSubject primary = command.getScope() != null ? command.getScope().getPrimary() : null;
        String entityName = primary != null && primary.getType() != null ? primary.getType().name() : "OTP";
        Long entityId = primary != null ? primary.getId() : null;
        String businessPurpose = "Send OTP for reference " + command.getReference().name();
        return new BusinessContext(entityName, entityId, businessPurpose);
    }
}
