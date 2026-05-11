package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.features.otp.core.enums.OtpChannel;
import com.nivasafinance.features.otp.core.exception.OtpExceptionFactory;
import com.nivasafinance.features.otp.core.service.OtpDeliveryService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.whatsapp.WhatsAppHandler;
import com.nivasafinance.services.whatsapp.dto.TemplateParameter;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WhatsAppOtpDeliveryService implements OtpDeliveryService {

    private final ServiceFactory<WhatsAppHandler> serviceFactory;

    @Override
    public OtpChannel getChannel() {
        return OtpChannel.WHATSAPP;
    }

    @Override
    public void send(String recipient, String otp, String templateName, String reference) {
        if (templateName == null || templateName.isBlank()) {
            throw OtpExceptionFactory.missingTemplateName();
        }

        String formattedPhoneNumber = formatRecipientPhone(recipient);
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
                buildBusinessContext(reference)
        );

        String status = response != null ? response.getStatus() : null;
        boolean success = status != null
                && ("sent".equalsIgnoreCase(status) || "accepted".equalsIgnoreCase(status));
        if (!success) {
            String errorMessage = response != null ? response.getErrorMessage() : null;
            String resolvedMessage = errorMessage != null && !errorMessage.isBlank()
                    ? errorMessage
                    : "Status: " + (status != null ? status : "null");
            throw OtpExceptionFactory.deliveryFailed("WhatsApp OTP send failed: " + resolvedMessage);
        }
    }

    private String formatRecipientPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw OtpExceptionFactory.missingRecipientPhone();
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

    private BusinessContext buildBusinessContext(String reference) {
        return new BusinessContext("OTP", null, "Send OTP for reference " + reference);
    }
}
