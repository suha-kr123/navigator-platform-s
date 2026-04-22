package com.nivasafinance.externals.whatsapp.service;

import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadResponse;

import java.util.Optional;
import java.util.UUID;

public interface WhatsAppLeadService {
    WhatsAppLeadResponse createOrGetLead(WhatsAppLeadRequest request);

    Optional<UUID> resolveLeadIdentifierByPrimaryMobileNumber(String mobileNumber);
}
