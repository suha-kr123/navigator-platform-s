package com.nivasafinance.externals.gallabox.service;

import com.nivasafinance.externals.gallabox.dto.WhatsAppNoteRequest;
import com.nivasafinance.externals.gallabox.dto.WhatsAppNoteResponse;

public interface WhatsAppNoteService {
    WhatsAppNoteResponse createNote(WhatsAppNoteRequest request);
}
