package com.nivasafinance.externals.gallabox.service;

import com.nivasafinance.externals.gallabox.dto.WhatsAppAdvisorRequest;
import com.nivasafinance.externals.gallabox.dto.WhatsAppAdvisorResponse;

public interface WhatsAppAdvisorService {
    WhatsAppAdvisorResponse createOrGetAdvisor(WhatsAppAdvisorRequest request);
}
