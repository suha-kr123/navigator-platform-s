package com.nivasafinance.externals.whatsapp.service;

import com.nivasafinance.externals.whatsapp.dto.WhatsAppStatusTrackerRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppStatusTrackerResponse;

public interface WhatsAppStatusTrackerService {
    WhatsAppStatusTrackerResponse trackStatus(WhatsAppStatusTrackerRequest request);
}
