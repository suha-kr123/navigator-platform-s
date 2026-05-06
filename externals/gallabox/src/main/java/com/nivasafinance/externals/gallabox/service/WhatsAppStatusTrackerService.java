package com.nivasafinance.externals.gallabox.service;

import com.nivasafinance.externals.gallabox.dto.WhatsAppStatusTrackerRequest;
import com.nivasafinance.externals.gallabox.dto.WhatsAppStatusTrackerResponse;

public interface WhatsAppStatusTrackerService {
    WhatsAppStatusTrackerResponse trackStatus(WhatsAppStatusTrackerRequest request);
}
