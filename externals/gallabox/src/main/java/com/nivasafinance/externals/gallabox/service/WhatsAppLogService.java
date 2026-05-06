package com.nivasafinance.externals.gallabox.service;

import com.nivasafinance.externals.gallabox.dto.WhatsAppLogRequest;
import com.nivasafinance.externals.gallabox.dto.WhatsAppLogResponse;

public interface WhatsAppLogService {
    WhatsAppLogResponse createWhatsAppLog(WhatsAppLogRequest request);
}
