package com.nivasafinance.externals.whatsapp.service;

import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadResponse;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;

public interface WhatsAppLeadService {
    WhatsAppLeadResponse createOrGetLead(CreateLeadRequest request);
}
