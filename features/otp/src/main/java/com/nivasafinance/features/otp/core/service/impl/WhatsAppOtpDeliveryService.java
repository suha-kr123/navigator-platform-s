package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.features.otp.core.dto.OtpRecipient;
import com.nivasafinance.features.otp.core.enums.OtpChannel;
import com.nivasafinance.features.otp.core.enums.OtpReference;
import com.nivasafinance.features.otp.core.service.OtpDeliveryService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.whatsapp.WhatsAppHandler;
import com.nivasafinance.services.whatsapp.dto.TemplateParameter;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
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
    public void send(OtpRecipient recipient, String otp, Integer validityInMins, OtpReference reference, String templateName) {
        WhatsAppHandler handler = serviceFactory.getHandler(ThirdPartyServiceList.WHATSAPP);
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber(recipient.getDestination())
                .templateName(templateName)
                .parameters(List.of(
                        TemplateParameter.builder().name("otp").value(otp).build(),
                        TemplateParameter.builder().name("validity_in_mins").value(String.valueOf(validityInMins)).build()))
                .build();
        BusinessContext context = new BusinessContext("LEAD", null, reference.name() + "_OTP");
        handler.sendTemplate(request, context);
    }
}
