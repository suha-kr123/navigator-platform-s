package com.nivasafinance.externals.whatsapp.service;

import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadResponse;

public interface WhatsAppLeadService {
    WhatsAppLeadResponse createOrGetLead(WhatsAppLeadRequest request);
}
