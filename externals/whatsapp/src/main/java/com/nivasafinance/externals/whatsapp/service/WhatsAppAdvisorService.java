package com.nivasafinance.externals.whatsapp.service;

import com.nivasafinance.externals.whatsapp.dto.WhatsAppAdvisorRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppAdvisorResponse;

public interface WhatsAppAdvisorService {
    WhatsAppAdvisorResponse createOrGetAdvisor(WhatsAppAdvisorRequest request);
}
