package com.nivasafinance.externals.whatsapp.service;

import com.nivasafinance.externals.whatsapp.dto.WhatsAppLogRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLogResponse;

public interface WhatsAppLogService {
    WhatsAppLogResponse createWhatsAppLog(WhatsAppLogRequest request);
}
